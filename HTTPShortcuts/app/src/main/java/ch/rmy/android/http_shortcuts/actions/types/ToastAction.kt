package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.utils.showToast
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.Variables
import com.android.volley.VolleyError

class ToastAction(
        id: String,
        actionType: ToastActionType,
        data: Map<String, String>
) : BaseAction(id, actionType, data) {

    var message: String
        get() = internalData[KEY_TEXT] ?: ""
        set(value) {
            internalData[KEY_TEXT] = value
        }

    override fun getDescription(context: Context): CharSequence =
            Variables.rawPlaceholdersToVariableSpans(context, context.getString(R.string.action_type_toast_description, message))

    override fun performBlocking(context: Context, shortcutId: Long, variableValues: MutableMap<String, String>, response: ShortcutResponse?, volleyError: VolleyError?, recursionDepth: Int) {
        val finalMessage = Variables.rawPlaceholdersToResolvedValues(message, variableValues)
        if (finalMessage.isNotEmpty()) {
            context.showToast(finalMessage, long = true)
        }
    }

    override fun createEditorView(context: Context, variablePlaceholderProvider: VariablePlaceholderProvider) =
            ToastActionEditorView(context, this, variablePlaceholderProvider)

    companion object {

        private const val KEY_TEXT = "text"

    }

}