package ch.rmy.android.http_shortcuts.scripting

import android.content.Context
import ch.rmy.android.http_shortcuts.actions.ActionDTO
import ch.rmy.android.http_shortcuts.actions.types.ActionFactory
import ch.rmy.android.http_shortcuts.data.RealmFactory
import ch.rmy.android.http_shortcuts.data.Repository
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.getShortcutResponse
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.variables.VariableManager
import com.android.volley.VolleyError
import io.reactivex.Completable
import org.liquidplayer.javascript.JSContext
import org.liquidplayer.javascript.JSFunction
import org.liquidplayer.javascript.JSValue

class ScriptExecutor(private val actionFactory: ActionFactory) {

    private val jsContext = JSContext()

    private var responseData: ShortcutResponse? = null
    private var volleyErrorData: VolleyError? = null

    init {
        registerActionAliases(actionFactory.getAliases())
    }

    private fun registerActionAliases(aliases: Map<String, ActionAlias>) {
        aliases.forEach { (actionName, alias) ->
            jsContext.evaluateScript("""
            const ${alias.functionName} = (${alias.parameters.joinToString()}) => {
                _runAction("$actionName", {
                    ${alias.parameters.joinToString { parameter -> "\"$parameter\": $parameter" }}
                });
            };
        """.trimIndent())
        }
    }

    fun execute(context: Context, script: String, shortcut: Shortcut, variableManager: VariableManager, response: ShortcutResponse? = null, volleyError: VolleyError? = null, recursionDepth: Int = 0): Completable =
        if (script.isEmpty()) {
            Completable.complete()
        } else {
            Completable.create { emitter ->
                jsContext.setExceptionHandler { exception ->
                    if (!emitter.isDisposed) {
                        emitter.onError(exception)
                    }
                }

                registerShortcut(shortcut)
                registerResponse(response, volleyError)
                registerVariables(variableManager)

                this.responseData = response
                this.volleyErrorData = volleyError
                registerActions(context, shortcut.id, variableManager, recursionDepth)

                jsContext.evaluateScript(script)
                emitter.onComplete()
            }
        }

    private fun registerShortcut(shortcut: Shortcut) {
        jsContext.property("shortcut", mapOf(
            "id" to shortcut.id,
            "name" to shortcut.name,
            "description" to shortcut.description
        ))
    }

    private fun registerResponse(response: ShortcutResponse?, volleyError: VolleyError?) {
        val responseObject = (response ?: volleyError?.getShortcutResponse())
        jsContext.property("response", responseObject?.let {
            mapOf(
                "body" to it.bodyAsString,
                "headers" to it.headers,
                "statusCode" to it.statusCode,
                "cookies" to it.cookies
            )
        })
        jsContext.property("networkError", volleyError?.message)
    }

    private fun registerVariables(variableManager: VariableManager) {
        jsContext.property("getVariable", object : JSFunction(jsContext, "run") {
            fun run(variableKeyOrId: String): String? =
                variableManager.getVariableValueByKeyOrId(variableKeyOrId)
        })
        jsContext.property("setVariable", object : JSFunction(jsContext, "run") {
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
        })
    }

    private fun registerActions(context: Context, shortcutId: String, variableManager: VariableManager, recursionDepth: Int) {
        jsContext.property("_runAction", object : JSFunction(jsContext, "run") {
            fun run(actionType: String, data: Map<String, JSValue>) {

                val action = actionFactory.fromDTO(ActionDTO(
                    type = actionType,
                    data = sanitizeData(data)
                ))

                action.perform(
                    context = context,
                    shortcutId = shortcutId,
                    variableManager = variableManager,
                    response = responseData,
                    volleyError = volleyErrorData,
                    recursionDepth = recursionDepth
                )
                    .blockingAwait()
            }
        }, JSContext.JSPropertyAttributeReadOnly or JSContext.JSPropertyAttributeDontDelete)
    }

    companion object {

        private fun sanitizeData(data: Map<String, JSValue?>): Map<String, String> =
            data.mapValues { sanitizeData(it.value) }

        private fun sanitizeData(data: JSValue?): String =
            data?.toString() ?: ""

    }

}