package ch.rmy.android.http_shortcuts.scripting

import android.content.Context
import ch.rmy.android.http_shortcuts.actions.ActionDTO
import ch.rmy.android.http_shortcuts.actions.types.ActionFactory
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import com.android.volley.VolleyError
import io.reactivex.Completable
import org.liquidplayer.javascript.JSContext
import org.liquidplayer.javascript.JSFunction
import org.liquidplayer.javascript.JSValue

class ScriptExecutor(private val actionFactory: ActionFactory) {

    private val jsContext = JSContext()

    private var responseData: ShortcutResponse? = null
    private var volleyErrorData: VolleyError? = null

    fun execute(context: Context, script: String, shortcutId: String, variableValues: MutableMap<String, String>, response: ShortcutResponse? = null, volleyError: VolleyError? = null, recursionDepth: Int = 0): Completable =
        Completable.create { emitter ->
            this.responseData = response

            jsContext.setExceptionHandler { exception ->
                if (!emitter.isDisposed) {
                    emitter.onError(exception)
                }
            }

            jsContext.property("getVariable", object : JSFunction(jsContext, "run") {
                fun run(variableName: String): String? = variableValues[variableName]
            })

            jsContext.property("_runAction", object : JSFunction(jsContext, "run") {
                fun run(actionType: String, data: Map<String, JSValue>) {

                    val action = actionFactory.fromDTO(ActionDTO(
                        type = actionType,
                        data = sanitizeData(data)
                    ))

                    action.perform(
                        context = context,
                        shortcutId = shortcutId,
                        variableValues = variableValues,
                        response = responseData,
                        volleyError = volleyErrorData,
                        recursionDepth = recursionDepth
                    )
                        .doOnComplete {
                            emitter.onComplete()
                        }
                        .doOnError { error ->
                            emitter.onError(error)
                        }
                        .blockingAwait()
                }
            }, JSContext.JSPropertyAttributeReadOnly or JSContext.JSPropertyAttributeDontDelete)

            jsContext.evaluateScript(script)
            emitter.onComplete()
        }

    private fun sanitizeData(data: Map<String, JSValue?>): Map<String, String> =
        data.mapValues { it.value?.toString() ?: "" }

}