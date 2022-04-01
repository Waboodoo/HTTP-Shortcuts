package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class RenameShortcutActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = RenameShortcutAction(
        shortcutNameOrId = actionDTO.getString(0)?.takeUnlessEmpty(),
        name = actionDTO.getString(1) ?: "",
    )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = 2,
    )

    companion object {
        private const val TYPE = "rename_shortcut"
        private const val FUNCTION_NAME = "renameShortcut"
    }
}
