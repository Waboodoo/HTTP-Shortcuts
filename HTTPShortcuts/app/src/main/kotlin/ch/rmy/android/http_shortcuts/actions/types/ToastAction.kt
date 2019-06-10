package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.extensions.showToast
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.Variables
import com.android.volley.VolleyError

class ToastAction(
    actionType: ToastActionType,
    data: Map<String, String>
) : BaseAction(actionType, data) {

    var message: String
        get() = internalData[KEY_TEXT] ?: ""
        set(value) {
            internalData[KEY_TEXT] = value
        }

    override fun performBlocking(context: Context, shortcutId: String, variableValues: MutableMap<String, String>, response: ShortcutResponse?, volleyError: VolleyError?, recursionDepth: Int) {
        val finalMessage = Variables.rawPlaceholdersToResolvedValues(message, variableValues)
        if (finalMessage.isNotEmpty()) {
            (context as ExecuteActivity).runOnUiThread {
                // TODO: Find a nicer way for running on the UI thread
                context.showToast(finalMessage, long = true)
            }
        }
    }

    override fun createEditorView(context: Context, variablePlaceholderProvider: VariablePlaceholderProvider) =
        ToastActionEditorView(context, this, variablePlaceholderProvider)

    companion object {

        private const val KEY_TEXT = "text"

    }

}