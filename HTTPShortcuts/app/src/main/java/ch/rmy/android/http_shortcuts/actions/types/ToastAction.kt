package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import android.widget.Toast
import ch.rmy.android.http_shortcuts.R

class ToastAction(id: String, actionType: ToastActionType, data: Map<String, String>) : BaseAction(id, actionType, data) {

    val message = data[KEY_TEXT] ?: ""

    override fun getDescription(context: Context) =
            context.getString(R.string.action_type_toast_description, message) // TODO: Include VariableSpans

    override fun performBlocking(context: Context, shortcutId: Long, variableValues: Map<String, String>) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show() // TODO: Include variables
    }

    companion object {

        private val KEY_TEXT = "text"

    }

}