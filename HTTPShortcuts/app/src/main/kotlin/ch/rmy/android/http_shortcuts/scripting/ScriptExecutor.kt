package ch.rmy.android.http_shortcuts.scripting

import android.content.Context
import androidx.annotation.Keep
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.actions.ActionDTO
import ch.rmy.android.http_shortcuts.actions.types.ActionFactory
import ch.rmy.android.http_shortcuts.data.RealmFactory
import ch.rmy.android.http_shortcuts.data.Repository
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.exceptions.CanceledByUserException
import ch.rmy.android.http_shortcuts.exceptions.JavaScriptException
import ch.rmy.android.http_shortcuts.extensions.logInfo
import ch.rmy.android.http_shortcuts.http.ErrorResponse
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.variables.VariableManager
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import org.liquidplayer.javascript.JSContext
import org.liquidplayer.javascript.JSException
import org.liquidplayer.javascript.JSFunction
import org.liquidplayer.javascript.JSValue

class ScriptExecutor(private val actionFactory: ActionFactory) {

    private val jsContext by lazy {
        JSContext()
            .also {
                registerActionAliases(it, actionFactory.getAliases())
            }
    }

    private var responseData: ShortcutResponse? = null
    private var responseErrorData: Exception? = null
    private var lastException: Throwable? = null

    fun execute(context: Context, script: String, shortcut: Shortcut, variableManager: VariableManager, response: ShortcutResponse? = null, error: Exception? = null, recursionDepth: Int = 0): Completable =
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
                registerVariables(variableManager)
                registerAbort()
                registerUserInteractions(context)

                this.responseData = response
                this.responseErrorData = error
                registerActions(context, shortcut.id, variableManager, recursionDepth)

                jsContext.evaluateScript(script)
                emitter.onComplete()
            }
                .onErrorResumeNext { e ->
                    Completable.error(
                        if (e is JSException) JavaScriptException(e) else e
                    )
                }
        }

    private fun registerShortcut(shortcut: Shortcut) {
        jsContext.property("shortcut", mapOf(
            "id" to shortcut.id,
            "name" to shortcut.name,
            "description" to shortcut.description
        ), READ_ONLY)
    }

    private fun registerResponse(response: ShortcutResponse?, error: Exception?) {
        val responseObject = (response ?: (error as? ErrorResponse)?.shortcutResponse)
        jsContext.property("response", responseObject?.let {
            mapOf(
                "body" to it.bodyAsString,
                "headers" to it.headers,
                "statusCode" to it.statusCode,
                "cookies" to it.cookies
            )
        }, READ_ONLY)
        jsContext.property("networkError", error?.message, READ_ONLY)
    }

    private fun registerVariables(variableManager: VariableManager) {
        jsContext.property("getVariable", object : JSFunction(jsContext, "run") {
            @Suppress("unused")
            @Keep
            fun run(variableKeyOrId: String): String? =
                variableManager.getVariableValueByKeyOrId(variableKeyOrId)
        }, READ_ONLY)
        jsContext.property("setVariable", object : JSFunction(jsContext, "run") {
            @Suppress("unused")
            @Keep
            fun run(variableKeyOrId: String, rawValue: JSValue?) {
                val value = sanitizeData(rawValue)
                variableManager.setVariableValueByKeyOrId(variableKeyOrId, value)

                // TODO: Handle variable persistance in a better way
                RealmFactory.getInstance().createRealm().use { realm ->
                    realm.executeTransaction {
                        Repository.getVariableByKeyOrId(realm, variableKeyOrId)
                            ?.takeIf { it.isConstant }
                            ?.value = value
                    }
                }
            }
        }, READ_ONLY)
    }

    private fun registerAbort() {
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
            fun run(actionType: String, data: Map<String, JSValue>): Boolean {
                logInfo("Running action of type: $actionType")
                val action = actionFactory.fromDTO(ActionDTO(
                    type = actionType,
                    data = sanitizeData(data)
                ))

                return try {
                    action
                        ?.perform(
                            context = context,
                            shortcutId = shortcutId,
                            variableManager = variableManager,
                            response = responseData,
                            responseError = responseErrorData as? ErrorResponse,
                            recursionDepth = recursionDepth
                        )
                        ?.blockingAwait()
                    true
                } catch (e: Throwable) {
                    lastException = if (e is RuntimeException && e.cause != null) e.cause else e
                    false
                }
            }
        }, READ_ONLY)
    }

    private fun registerUserInteractions(context: Context) {
        jsContext.property("alert", object : JSFunction(jsContext, "run") {
            @Suppress("unused")
            @Keep
            fun run(message: String) {
                Completable.create { emitter ->
                    DialogBuilder(context)
                        .message(message)
                        .positive(R.string.dialog_ok)
                        .dismissListener { emitter.onComplete() }
                        .show()
                }
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .blockingAwait()
            }
        }, READ_ONLY)
        jsContext.property("confirm", object : JSFunction(jsContext, "run") {
            @Suppress("unused")
            @Keep
            fun run(message: String?): Boolean =
                Single.create<Boolean> { emitter ->
                    DialogBuilder(context)
                        .message(message ?: "")
                        .positive(R.string.dialog_ok) {
                            emitter.onSuccess(true)
                        }
                        .negative(R.string.dialog_cancel) {
                            emitter.onSuccess(false)
                        }
                        .dismissListener { emitter.onSuccess(false) }
                        .show()
                }
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .blockingGet()
        }, READ_ONLY)
        jsContext.property("prompt", object : JSFunction(jsContext, "run") {
            @Suppress("unused")
            @Keep
            fun run(message: String?, default: String?): String? =
                Single.create<String> { emitter ->
                    DialogBuilder(context)
                        .message(message ?: "")
                        .textInput(prefill = default ?: "") { input ->
                            emitter.onSuccess("-$input")
                        }
                        .dismissListener { emitter.onSuccess("") }
                        .show()
                }
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .blockingGet()
                    .takeUnless { it.isEmpty() }
                    ?.removePrefix("-")
        }, READ_ONLY)
    }

    companion object {

        private const val READ_ONLY =
            JSContext.JSPropertyAttributeReadOnly or JSContext.JSPropertyAttributeDontDelete

        private fun registerActionAliases(jsContext: JSContext, aliases: Map<String, ActionAlias>) {
            aliases
                .forEach { (actionName, alias) ->
                    jsContext.evaluateScript(
                        """
                        const ${alias.functionName} = (${alias.parameters.joinToString()}) => {
                            const actionSuccess = _runAction("$actionName", {
                                ${alias.parameters.joinToString { parameter -> "\"$parameter\": $parameter" }}
                            });
                            if (!actionSuccess) {
                                throw "Error";
                            }
                        };
                        """.trimIndent()
                    )
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