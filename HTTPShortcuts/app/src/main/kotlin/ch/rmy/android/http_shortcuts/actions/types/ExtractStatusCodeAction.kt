package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.data.Commons
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.variables.VariableManager
import com.android.volley.VolleyError
import io.reactivex.Completable

@Deprecated("Will be removed eventually")
class ExtractStatusCodeAction(
    actionType: ExtractStatusCodeActionType,
    data: Map<String, String>
) : BaseAction(actionType, data) {

    var variableId: String
        get() = internalData[KEY_VARIABLE_ID] ?: ""
        set(value) {
            internalData[KEY_VARIABLE_ID] = value
        }

    override fun perform(context: Context, shortcutId: String, variableManager: VariableManager, response: ShortcutResponse?, volleyError: VolleyError?, recursionDepth: Int): Completable {
        val statusCode = response?.statusCode
            ?: volleyError?.networkResponse?.statusCode
            ?: return Completable.complete()
        val statusCodeString = statusCode.toString()
        variableManager.setVariableValueById(variableId, statusCodeString)
        return Commons.setVariableValue(variableId, statusCodeString)
    }

    companion object {

        private const val KEY_VARIABLE_ID = "variableId"

    }

}