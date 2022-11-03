package ch.rmy.android.http_shortcuts.scripting

import android.content.Context
import androidx.annotation.Keep
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.resume
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.exceptions.JavaScriptException
import ch.rmy.android.http_shortcuts.http.ErrorResponse
import ch.rmy.android.http_shortcuts.http.FileUploadManager
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO
import ch.rmy.android.http_shortcuts.scripting.actions.ActionFactory
import ch.rmy.android.http_shortcuts.variables.VariableManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.liquidplayer.javascript.JSContext
import org.liquidplayer.javascript.JSException
import org.liquidplayer.javascript.JSFunction
import org.liquidplayer.javascript.JSUint8Array
import org.liquidplayer.javascript.JSValue
import javax.inject.Inject
import kotlin.coroutines.resumeWithException

class ScriptExecutor
@Inject
constructor(
    private val context: Context,
    private val actionFactory: ActionFactory,
    private val responseObjectFactory: ResponseObjectFactory,
) {

    private val jsContext by lazy(LazyThreadSafetyMode.NONE) {
        JSContext()
            .also {
                registerActionAliases(it, actionFactory.getAliases())
                registerAbort(it)
            }
    }

    private var lastException: Throwable? = null

    suspend fun initialize(
        shortcut: ShortcutModel,
        variableManager: VariableManager,
        fileUploadResult: FileUploadManager.Result?,
        recursionDepth: Int = 0,
    ) {
        runWithExceptionHandling {
            registerShortcut(shortcut)
            registerFiles(fileUploadResult)
            registerActions(context, shortcut.id, variableManager, recursionDepth)
        }
    }

    private suspend fun runWithExceptionHandling(block: () -> Unit) {
        try {
            suspendCancellableCoroutine<Unit> { continuation ->
                jsContext.setExceptionHandler { exception ->
                    if (continuation.isActive) {
                        continuation.resumeWithException(lastException ?: exception)
                    }
                }
                block()
                if (continuation.isActive) {
                    continuation.resume()
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw when (e) {
                is JSException -> if (e.error.message() == "java.lang.reflect.InvocationTargetException") {
                    JavaScriptException("Invalid function arguments", e)
                } else {
                    JavaScriptException(e)
                }
                is JSONException -> JavaScriptException(e)
                else -> e
            }
        }
    }

    suspend fun execute(
        script: String,
        response: ShortcutResponse? = null,
        error: Exception? = null,
    ) {
        if (script.isEmpty()) {
            return
        }
        withContext(Dispatchers.Default) {
            ensureActive()
            runWithExceptionHandling {
                registerResponse(response, error)
                jsContext.evaluateScript(script)
            }
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
        (response ?: (error as? ErrorResponse)?.shortcutResponse)
            ?.let { responseObject ->
                responseObjectFactory.create(jsContext, responseObject)
            }
            .let {
                jsContext.property("response", it, READ_ONLY)
            }
        jsContext.property("networkError", error?.message, READ_ONLY)
    }

    private fun registerFiles(fileUploadResult: FileUploadManager.Result?) {
        jsContext.property(
            "selectedFiles",
            fileUploadResult?.getFiles()
                ?.map { file ->
                    mapOf(
                        "id" to file.id,
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
                    lastException = CancellationException()
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
                fun run(actionType: String, rawData: JSValue?): JSValue? {
                    logInfo("Running action of type: $actionType")

                    val data = when {
                        rawData?.isArray == true -> {
                            rawData
                                .toJSArray()
                                .toList()
                                .map { it as? JSValue }
                        }
                        rawData?.isObject == true -> {
                            // Legacy support
                            rawData
                                .toObject()
                                .let { obj ->
                                    obj.propertyNames()
                                        .map(obj::property)
                                }
                        }
                        else -> emptyList()
                    }
                    val action = actionFactory.fromDTO(
                        ActionDTO(
                            type = actionType,
                            data = data,
                        )
                    )
                        ?: return null

                    return try {
                        val result = runBlocking {
                            action.run(
                                ExecutionContext(
                                    context = context,
                                    jsContext = jsContext,
                                    shortcutId = shortcutId,
                                    variableManager = variableManager,
                                    recursionDepth = recursionDepth,
                                )
                            )
                        }
                        convertResult(jsContext, result)
                    } catch (e: CancellationException) {
                        lastException = e
                        null
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

        private const val NO_RESULT = "[[[no result]]]"

        private const val READ_ONLY =
            JSContext.JSPropertyAttributeReadOnly or JSContext.JSPropertyAttributeDontDelete

        private fun registerActionAliases(jsContext: JSContext, aliases: Map<String, ActionAlias>) {
            jsContext.evaluateScript(
                """
                const _convertResult = (result) => {
                    if (result === null || result === undefined) {
                        throw "Error";
                    } else if (result === "$NO_RESULT") {
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

        private fun convertResult(jsContext: JSContext, result: Any?): JSValue =
            when (result) {
                is ByteArray -> JSUint8Array(jsContext, result.size)
                    .apply {
                        result.forEachIndexed { index, byte ->
                            set(index, byte)
                        }
                    }
                else -> JSValue(jsContext, result ?: NO_RESULT)
            }
    }
}
