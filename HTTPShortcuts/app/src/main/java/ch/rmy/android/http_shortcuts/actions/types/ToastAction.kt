package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import android.widget.Toast
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.actions.ActionDTO

class ToastAction(action: ActionDTO) : BaseAction(action) {

    val message = action.data[KEY_TEXT] ?: ""

    override fun getTitle(context: Context) =
            context.getString(R.string.action_type_toast_title)

    override fun getDescription(context: Context) =
            context.getString(R.string.action_type_toast_description, message) // TODO: Include VariableSpans

    override fun performBlocking(context: Context, shortcutId: Long, variableValues: Map<String, String>) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show() // TODO: Include variables
    }

    companion object {

        const val TYPE = "show_toast"

        private val KEY_TEXT = "text"

    }

}