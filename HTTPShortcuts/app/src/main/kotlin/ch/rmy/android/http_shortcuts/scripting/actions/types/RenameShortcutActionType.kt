package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class RenameShortcutActionType(context: Context) : BaseActionType(context) {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = RenameShortcutAction(this, actionDTO.data)

    override fun getAlias() = ActionAlias(
        functionName = "renameShortcut",
        parameters = listOf(RenameShortcutAction.KEY_SHORTCUT_NAME_OR_ID, RenameShortcutAction.KEY_NAME)
    )

    companion object {

        const val TYPE = "rename_shortcut"

    }

}