package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.Variables
import com.android.volley.VolleyError
import org.jdeferred2.Promise

class SetVariableAction(
        id: String,
        actionType: SetVariableActionType,
        data: Map<String, String>
) : BaseAction(id, actionType, data) {

    var newValue: String
        get() = internalData[KEY_NEW_VALUE] ?: ""
        set(value) {
            internalData[KEY_NEW_VALUE] = value
        }

    var variableKey: String
        get() = internalData[KEY_VARIABLE_KEY] ?: ""
        set(value) {
            internalData[KEY_VARIABLE_KEY] = value
        }

    override fun getDescription(context: Context): CharSequence =
            Variables.rawPlaceholdersToVariableSpans(
                    context,
                    context.getString(R.string.action_type_set_variable_description, Variables.toRawPlaceholder(variableKey), newValue)
            )

    override fun perform(context: Context, shortcutId: Long, variableValues: MutableMap<String, String>, response: ShortcutResponse?, volleyError: VolleyError?, recursionDepth: Int): Promise<Unit, Throwable, Unit> {
        val value = Variables.rawPlaceholdersToResolvedValues(newValue, variableValues)
        variableValues[variableKey] = value
        Controller().use { controller ->
            return controller.setVariableValue(variableKey, value)
        }
    }

    override fun createEditorView(context: Context, variablePlaceholderProvider: VariablePlaceholderProvider) =
            SetVariableActionEditorView(context, this, variablePlaceholderProvider)

    companion object {

        private const val KEY_NEW_VALUE = "newValue"
        private const val KEY_VARIABLE_KEY = "variableKey"

    }

}