package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.actions.ActionDTO
import ch.rmy.android.http_shortcuts.scripting.ActionAlias

class RenameShortcutActionType(context: Context) : BaseActionType(context) {

    override val type = TYPE

    override val title: String = context.getString(R.string.action_type_rename_shortcut_title)

    override fun fromDTO(actionDTO: ActionDTO) = RenameShortcutAction(this, actionDTO.data)

    override fun getAlias() = ActionAlias(
        functionName = "renameShortcut",
        parameters = listOf(RenameShortcutAction.KEY_SHORTCUT_NAME_OR_ID, RenameShortcutAction.KEY_NAME)
    )

    companion object {

        const val TYPE = "rename_shortcut"

    }

}