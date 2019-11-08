package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.data.Commons
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.Variables
import com.android.volley.VolleyError
import io.reactivex.Completable

@Deprecated("Will be removed eventually")
class SetVariableAction(
    actionType: SetVariableActionType,
    data: Map<String, String>
) : BaseAction(actionType, data) {

    var newValue: String
        get() = internalData[KEY_NEW_VALUE] ?: ""
        set(value) {
            internalData[KEY_NEW_VALUE] = value
        }

    var variableId: String
        get() = internalData[KEY_VARIABLE_ID] ?: ""
        set(value) {
            internalData[KEY_VARIABLE_ID] = value
        }

    override fun perform(context: Context, shortcutId: String, variableManager: VariableManager, response: ShortcutResponse?, volleyError: VolleyError?, recursionDepth: Int): Completable {
        val value = Variables.rawPlaceholdersToResolvedValues(newValue, variableManager.getVariableValuesByIds())
        variableManager.setVariableValueById(variableId, value)
        return Commons.setVariableValue(variableId, value)
    }

    companion object {

        private const val KEY_NEW_VALUE = "newValue"
        private const val KEY_VARIABLE_ID = "variableId"

    }

}