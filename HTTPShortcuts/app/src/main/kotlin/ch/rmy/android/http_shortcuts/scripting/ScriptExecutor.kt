package ch.rmy.android.http_shortcuts.scripting

import android.content.Context
import androidx.annotation.Keep
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.exceptions.CanceledByUserException
import ch.rmy.android.http_shortcuts.exceptions.JavaScriptException
import ch.rmy.android.http_shortcuts.exceptions.ResponseTooLargeException
import ch.rmy.android.http_shortcuts.extensions.logInfo
import ch.rmy.android.http_shortcuts.http.ErrorResponse
import ch.rmy.android.http_shortcuts.http.FileUploadManager
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO
import ch.rmy.android.http_shortcuts.scripting.actions.types.ActionFactory
import ch.rmy.android.http_shortcuts.scripting.actions.types.BaseAction
import ch.rmy.android.http_shortcuts.variables.VariableManager
import io.reactivex.Completable
import org.json.JSONException
import org.liquidplayer.javascript.JSContext
import org.liquidplayer.javascript.JSException
import org.liquidplayer.javascript.JSFunction
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
        shortcut: Shortcut,
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
                            is JSException -> JavaScriptException(e)
                            is JSONException -> JavaScriptException(e)
                            else -> e
                        }
                    )
                }
        }

    private fun registerShortcut(shortcut: Shortcut) {
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
        val responseObject = (response ?: (error as? ErrorResponse)?.shortcutResponse)
        jsContext.property(
            "response",
            responseObject?.let {
                mapOf(
                    "body" to try {
                        it.getContentAsString(context)
                    } catch (e: ResponseTooLargeException) {
                        ""
                    },
                    "headers" to it.headersAsMap,
                    "statusCode" to it.statusCode,
                    "cookies" to it.cookies,
                )
            },
            READ_ONLY,
        )
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
        jsContext.property("_abort", object : JSFunction(jsContext, "run") {
            @Suppress("unused")
            @Keep
            fun run() {
                lastException = CanceledByUserException()
            }
        }, READ_ONLY)
    }

    private fun registerActions(context: Context, shortcutId: String, variableManager: VariableManager, recursionDepth: Int) {
        jsContext.property("_runAction", object : JSFunction(jsContext, "run") {
            @Suppress("unused")
            @Keep
            fun run(actionType: String, data: Map<String, JSValue>): String? {
                logInfo("Running action of type: $actionType")
                val action = actionFactory.fromDTO(ActionDTO(
                    type = actionType,
                    data = sanitizeData(data)
                ))

                return try {
                    action
                        ?.executeForValue(ExecutionContext(
                            context = context,
                            shortcutId = shortcutId,
                            variableManager = variableManager,
                            recursionDepth = recursionDepth
                        ))
                        ?.blockingGet()
                } catch (e: Throwable) {
                    lastException = if (e is RuntimeException && e.cause != null) e.cause else e
                    null
                }
            }
        }, READ_ONLY)
    }

    companion object {

        private const val READ_ONLY =
            JSContext.JSPropertyAttributeReadOnly or JSContext.JSPropertyAttributeDontDelete

        private fun registerActionAliases(jsContext: JSContext, aliases: Map<String, ActionAlias>) {
            jsContext.evaluateScript(
                """
                const _convertResult = (result, returnType) => {
                    if (result === null || result === undefined) {
                        throw "Error";
                    } else if (result === "${BaseAction.NO_RESULT}") {
                        return null;
                    } else if (returnType === "${ActionAlias.ReturnType.BOOLEAN}") {
                        return result === "${true}";    
                    } else {
                        return result;
                    }
                };
                """.trimIndent()
            )
            aliases
                .forEach { (actionName, alias) ->
                    jsContext.evaluateScript(
                        """
                        const ${alias.functionName} = (${alias.parameters.joinToString()}) => {
                            const result = _runAction("$actionName", {
                                ${
                            alias.parameters.joinToString { parameter ->
                                // Cast numbers to strings to avoid rounding errors
                                "\"$parameter\": typeof($parameter) === 'number' ? `\${$parameter}` : $parameter"
                            }
                        }
                            });
                            return _convertResult(result, "${alias.returnType.name}");
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

        private fun sanitizeData(data: Map<String, JSValue?>): Map<String, String> =
            data.mapValues { sanitizeData(it.value) }

        private fun sanitizeData(data: JSValue?): String =
            when {
                data == null || data.isNull || data.isUndefined -> ""
                data.isObject || data.isArray -> data.toJSON()
                else -> data.toString()
            }

    }

}