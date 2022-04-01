package ch.rmy.android.http_shortcuts.scripting

import android.content.Context
import androidx.annotation.Keep
import ch.rmy.android.framework.extensions.getCaseInsensitive
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.tryOrLog
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.exceptions.CanceledByUserException
import ch.rmy.android.http_shortcuts.exceptions.JavaScriptException
import ch.rmy.android.http_shortcuts.exceptions.ResponseTooLargeException
import ch.rmy.android.http_shortcuts.http.ErrorResponse
import ch.rmy.android.http_shortcuts.http.FileUploadManager
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO
import ch.rmy.android.http_shortcuts.scripting.actions.ActionFactory
import ch.rmy.android.http_shortcuts.scripting.actions.types.BaseAction
import ch.rmy.android.http_shortcuts.variables.VariableManager
import io.reactivex.Completable
import org.json.JSONException
import org.liquidplayer.javascript.JSContext
import org.liquidplayer.javascript.JSException
import org.liquidplayer.javascript.JSFunction
import org.liquidplayer.javascript.JSObject
import org.liquidplayer.javascript.JSUint8Array
import org.liquidplayer.javascript.JSValue

class ScriptExecutor(private val context: Context, private val actionFactory: ActionFactory) {

    private val jsContext by lazy {
        JSContext()
            .also {
                registerActionAliases(it, actionFactory.getAliases())
                registerAbort(it)
            }
    }

    private var lastException: Throwable? = null

    fun execute(
        script: String,
        shortcut: ShortcutModel,
        variableManager: VariableManager,
        fileUploadManager: FileUploadManager?,
        response: ShortcutResponse? = null,
        error: Exception? = null,
        recursionDepth: Int = 0,
    ): Completable =
        if (script.isEmpty()) {
            Completable.complete()
        } else {
            Completable.create { emitter ->
                jsContext.setExceptionHandler { exception ->
                    if (!emitter.isDisposed) {
                        emitter.onError(lastException ?: exception)
                    }
                }

                registerShortcut(shortcut)
                registerResponse(response, error)
                registerFiles(fileUploadManager)

                registerActions(context, shortcut.id, variableManager, recursionDepth)

                jsContext.evaluateScript(script)
                emitter.onComplete()
            }
                .onErrorResumeNext { e ->
                    Completable.error(
                        when (e) {
                            is JSException -> if (e.error.message() == "java.lang.reflect.InvocationTargetException") {
                                JavaScriptException("Invalid function arguments", e)
                            } else {
                                JavaScriptException(e)
                            }
                            is JSONException -> JavaScriptException(e)
                            else -> e
                        }
                    )
                }
        }

    private fun registerShortcut(shortcut: ShortcutModel) {
        jsContext.property(
            "shortcut",
            mapOf(
                "id" to shortcut.id,
                "name" to shortcut.name,
                "description" to shortcut.description,
            ),
            READ_ONLY,
        )
    }

    private fun registerResponse(response: ShortcutResponse?, error: Exception?) {
        if (response == null && error == null) {
            return
        }
        (response ?: (error as? ErrorResponse)?.shortcutResponse)?.let { responseObject ->
            val responseJsObject = JSObject(jsContext)
            responseJsObject.property(
                "body",
                try {
                    responseObject.getContentAsString(context)
                } catch (e: ResponseTooLargeException) {
                    ""
                }
            )
            responseJsObject.property("headers", tryOrLog { responseObject.headersAsMultiMap }, READ_ONLY)
            responseJsObject.property("cookies", tryOrLog { responseObject.cookiesAsMultiMap }, READ_ONLY)
            responseJsObject.property("statusCode", responseObject.statusCode, READ_ONLY)
            responseJsObject.property(
                "getHeader",
                object : JSFunction(jsContext, "run") {
                    @Suppress("unused")
                    @Keep
                    fun run(headerName: String): String? =
                        responseObject.headers.getLast(headerName)
                },
                READ_ONLY,
            )
            responseJsObject.property(
                "getCookie",
                object : JSFunction(jsContext, "run") {
                    @Suppress("unused")
                    @Keep
                    fun run(cookieName: String): String? =
                        responseObject.cookiesAsMultiMap.getCaseInsensitive(cookieName)?.last()
                },
                READ_ONLY,
            )
            jsContext.property("response", responseJsObject, READ_ONLY)
        }
        jsContext.property("networkError", error?.message, READ_ONLY)
    }

    private fun registerFiles(fileUploadManager: FileUploadManager?) {
        jsContext.property(
            "selectedFiles",
            fileUploadManager?.getFiles()
                ?.map { file ->
                    mapOf(
                        "name" to file.fileName,
                        "size" to file.fileSize,
                        "type" to file.mimeType,
                    )
                }
                ?: emptyList<Map<String, String>>(),
            READ_ONLY,
        )
    }

    private fun registerAbort(jsContext: JSContext) {
        jsContext.evaluateScript(
            """
            function abort() {
                _abort();
                throw "Abort";
            }
            """.trimIndent()
        )
        jsContext.property(
            "_abort",
            object : JSFunction(jsContext, "run") {
                @Suppress("unused")
                @Keep
                fun run() {
                    lastException = CanceledByUserException()
                }
            },
            READ_ONLY,
        )
    }

    private fun registerActions(context: Context, shortcutId: ShortcutId, variableManager: VariableManager, recursionDepth: Int) {
        jsContext.property(
            "_runAction",
            object : JSFunction(jsContext, "run") {
                @Suppress("unused")
                @Keep
                fun run(actionType: String, data: List<JSValue?>): JSValue? {
                    logInfo("Running action of type: $actionType")
                    val action = actionFactory.fromDTO(
                        ActionDTO(
                            type = actionType,
                            data = data,
                        )
                    )

                    return try {
                        action
                            ?.executeForValue(
                                ExecutionContext(
                                    context = context,
                                    shortcutId = shortcutId,
                                    variableManager = variableManager,
                                    recursionDepth = recursionDepth,
                                )
                            )
                            ?.blockingGet()
                            ?.let { result ->
                                convertResult(jsContext, result)
                            }
                    } catch (e: Throwable) {
                        lastException = if (e is RuntimeException && e.cause != null) e.cause else e
                        null
                    }
                }
            },
            READ_ONLY
        )
    }

    companion object {

        private const val READ_ONLY =
            JSContext.JSPropertyAttributeReadOnly or JSContext.JSPropertyAttributeDontDelete

        private fun registerActionAliases(jsContext: JSContext, aliases: Map<String, ActionAlias>) {
            jsContext.evaluateScript(
                """
                const _convertResult = (result) => {
                    if (result === null || result === undefined) {
                        throw "Error";
                    } else if (result === "${BaseAction.NO_RESULT}") {
                        return null;
                    } else {
                        return result;
                    }
                };
                """.trimIndent()
            )
            aliases
                .forEach { (actionName, alias) ->
                    val parameterNames = (0 until alias.parameters).map { "param$it" }
                    jsContext.evaluateScript(
                        """
                        const ${alias.functionName} = (${parameterNames.joinToString()}) => {
                            const result = _runAction("$actionName", [
                                ${
                        parameterNames.joinToString { parameter ->
                            // Cast numbers to strings to avoid rounding errors
                            "typeof($parameter) === 'number' ? `\${$parameter}` : $parameter"
                        }
                        }
                            ]);
                            return _convertResult(result);
                        };
                        """.trimIndent()
                    )
                    alias.functionNameAliases.forEach {
                        jsContext.evaluateScript(
                            """
                            const $it = ${alias.functionName};
                            """.trimIndent()
                        )
                    }
                }
        }

        private fun convertResult(jsContext: JSContext, result: Any?): JSValue? =
            when (result) {
                null -> null
                is ByteArray -> JSUint8Array(jsContext, result.size)
                    .apply {
                        result.forEachIndexed { index, byte ->
                            set(index, byte)
                        }
                    }
                else -> JSValue(jsContext, result)
            }
    }
}
