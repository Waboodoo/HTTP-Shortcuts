package ch.rmy.android.http_shortcuts.scripting

import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.resume
import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.exceptions.JavaScriptException
import ch.rmy.android.http_shortcuts.exceptions.TreatAsFailureException
import ch.rmy.android.http_shortcuts.exceptions.UserAbortException
import ch.rmy.android.http_shortcuts.http.ErrorResponse
import ch.rmy.android.http_shortcuts.http.FileUploadManager
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.scripting.actions.ActionFactory
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.scripting.JsFunction
import ch.rmy.android.scripting.JsFunctionArgs
import ch.rmy.android.scripting.JsObject
import ch.rmy.android.scripting.ScriptingEngine
import ch.rmy.android.scripting.ScriptingEngineFactory
import ch.rmy.android.scripting.ScriptingException
import javax.inject.Inject
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONException

class ScriptExecutor
@Inject
constructor(
    private val scriptingEngineFactory: ScriptingEngineFactory,
    private val actionFactory: ActionFactory,
    private val responseObjectFactory: ResponseObjectFactory,
    private val codeTransformer: CodeTransformer,
) {
    private val scriptingEngine: ScriptingEngine by lazy(LazyThreadSafetyMode.NONE) {
        scriptingEngineFactory.create()
            .also {
                registerActionAliases(it, actionFactory.getAliases())
                registerAbort(it)
                destroyer = it::destroy
            }
    }
    private var destroyer: (() -> Unit)? = null

    private val cleanupHandler = CleanupHandler()

    private var lastException: Throwable? = null

    suspend fun initialize(
        shortcut: Shortcut,
        category: Category,
        variableManager: VariableManager,
        fileUploadResult: FileUploadManager.Result?,
        resultHandler: ResultHandler,
        dialogHandle: DialogHandle,
        recursionDepth: Int = 0,
    ) {
        runWithExceptionHandling {
            registerShortcut(shortcut, category)
            registerFiles(fileUploadResult)
            registerActions(shortcut.id, variableManager, resultHandler, dialogHandle, recursionDepth)
        }
    }

    private suspend fun runWithExceptionHandling(block: () -> Unit) {
        lastException = null
        try {
            suspendCancellableCoroutine<Unit> { continuation ->
                continuation.invokeOnCancellation {
                    cleanupHandler.finally()
                }
                scriptingEngine.setExceptionHandler { exception ->
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
        } catch (e: ScriptingException) {
            throw lastException ?: JavaScriptException(e.message, e.lineNumber)
        } catch (e: JSONException) {
            throw JavaScriptException(e.message)
        } finally {
            cleanupHandler.finally()
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
        runWithExceptionHandling {
            if (response != null) {
                registerAbortAndTreatAsFailure()
            }
            registerResponse(response, error)
            scriptingEngine.evaluateScript(codeTransformer.transformForExecuting(script))
        }
    }

    private fun registerShortcut(shortcut: Shortcut, category: Category) {
        scriptingEngine.registerObject(
            "shortcut",
            scriptingEngine.buildJsObject {
                property("id", shortcut.id)
                property("name", shortcut.name)
                property("description", shortcut.description)
                property("hidden", shortcut.hidden)
                property(
                    "category",
                    scriptingEngine.buildJsObject {
                        property("id", category.id)
                        property("name", category.name)
                    },
                )
            },
        )
    }

    private fun registerResponse(response: ShortcutResponse?, error: Exception?) {
        if (response == null && error == null) {
            return
        }
        (response ?: (error as? ErrorResponse)?.shortcutResponse)
            ?.let { responseObject ->
                responseObjectFactory.create(scriptingEngine, responseObject)
            }
            .let {
                scriptingEngine.registerObject("response", it)
            }
        scriptingEngine.registerString("networkError", error?.message)
    }

    private fun registerFiles(fileUploadResult: FileUploadManager.Result?) {
        scriptingEngine.registerListOfObjects(
            "selectedFiles",
            fileUploadResult?.getFiles()
                ?.map { file ->
                    scriptingEngine.buildJsObject {
                        property("id", file.id)
                        property("name", file.fileName)
                        property("size", file.fileSize)
                        property("type", file.mimeType)
                        property(
                            "meta",
                            scriptingEngine.buildJsObject {
                                file.metaData?.let {
                                    property("orientation", it.orientation)
                                    property("created", it.created)
                                }
                            },
                        )
                    }
                }
                ?: emptyList<JsObject>(),
        )
    }

    private fun registerAbort(engine: ScriptingEngine) {
        engine.evaluateScript(
            """
            function abort() {
                __abort(0);
                throw "Abort";
            }
            function abortAll() {
                __abort(1);
                throw "Abort";
            }
            """.trimIndent(),
        )
        engine.registerFunction(
            "__abort",
            object : JsFunction {
                override fun invoke(args: JsFunctionArgs): Any? {
                    val abortType = args.getInt(0)
                    val message = args.getString(1)
                    lastException = when (abortType) {
                        2 -> TreatAsFailureException(message?.takeUnless { it == "undefined" })
                        1 -> UserAbortException(abortAll = true)
                        else -> UserAbortException(abortAll = false)
                    }
                    return null
                }
            },
        )
    }

    private fun registerAbortAndTreatAsFailure() {
        scriptingEngine.evaluateScript(
            """
            function abortAndTreatAsFailure(message) {
                __abort(2, message);
                throw "Abort";
            }
            """.trimIndent(),
        )
    }

    private fun registerActions(
        shortcutId: ShortcutId,
        variableManager: VariableManager,
        resultHandler: ResultHandler,
        dialogHandle: DialogHandle,
        recursionDepth: Int,
    ) {
        scriptingEngine.registerFunction(
            "_runAction",
            object : JsFunction {
                override fun invoke(args: JsFunctionArgs): Any? {
                    val actionTypeName = args.getString(0)!!
                    val data = args.getJsFunctionArgs(1)!!
                    logInfo("Running action of type: $actionTypeName")

                    val actionType = actionFactory.getType(actionTypeName)
                        ?: return null
                    val runnable = actionType.getActionRunnable(data)

                    return try {
                        runBlocking {
                            runnable.run(
                                ExecutionContext(
                                    scriptingEngine = scriptingEngine,
                                    shortcutId = shortcutId,
                                    variableManager = variableManager,
                                    resultHandler = resultHandler,
                                    recursionDepth = recursionDepth,
                                    dialogHandle = dialogHandle,
                                    cleanupHandler = cleanupHandler,
                                    onException = { e ->
                                        lastException = e
                                        throw e
                                    },
                                ),
                            )
                        }
                            ?: NO_RESULT
                    } catch (e: CancellationException) {
                        lastException = e
                        null
                    } catch (e: Throwable) {
                        lastException = if (e is RuntimeException && e.cause != null) e.cause else e
                        throw e
                    }
                }
            },
        )
    }

    fun destroy() {
        destroyer?.invoke()
    }

    companion object {

        private const val NO_RESULT = "[[[no result]]]"

        internal fun registerActionAliases(engine: ScriptingEngine, aliases: Map<String, ActionAlias>) {
            engine.evaluateScript(
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
                """.trimIndent(),
            )
            aliases
                .forEach { (actionName, alias) ->
                    val parameterNames = (0 until alias.parameters).map { "param$it" }
                    engine.evaluateScript(
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
                        """.trimIndent(),
                    )
                    alias.functionNameAliases.forEach {
                        engine.evaluateScript(
                            """
                            const $it = ${alias.functionName};
                            """.trimIndent(),
                        )
                    }
                }
        }
    }
}
