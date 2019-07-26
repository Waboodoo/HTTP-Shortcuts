package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.actions.ActionDTO
import ch.rmy.android.http_shortcuts.scripting.ActionAlias

class CopyToClipboardActionType(context: Context) : BaseActionType(context) {

    override val type = TYPE

    override val title: String = context.getString(R.string.action_copy_to_clipboard_title)

    override fun fromDTO(actionDTO: ActionDTO) = CopyToClipboardAction(this, actionDTO.data)

    override fun getAlias() = ActionAlias(
        functionName = "copyToClipboard",
        parameters = listOf(CopyToClipboardAction.KEY_TEXT)
    )

    companion object {

        const val TYPE = "copy_to_clipboard"

    }

}