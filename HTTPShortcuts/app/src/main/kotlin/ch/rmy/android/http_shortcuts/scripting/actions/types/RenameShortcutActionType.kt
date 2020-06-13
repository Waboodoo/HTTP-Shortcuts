package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class RenameShortcutActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = RenameShortcutAction(actionDTO.data)

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = listOf(RenameShortcutAction.KEY_SHORTCUT_NAME_OR_ID, RenameShortcutAction.KEY_NAME)
    )

    companion object {

        const val TYPE = "rename_shortcut"
        const val FUNCTION_NAME = "renameShortcut"

    }

}