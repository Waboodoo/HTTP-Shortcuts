package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class RenameShortcutActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = RenameShortcutAction(
        name = actionDTO.getString(KEY_NAME) ?: "",
        shortcutNameOrId = actionDTO.getString(KEY_SHORTCUT_NAME_OR_ID)?.takeUnlessEmpty(),
    )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = listOf(KEY_SHORTCUT_NAME_OR_ID, KEY_NAME),
    )

    companion object {

        const val TYPE = "rename_shortcut"
        const val FUNCTION_NAME = "renameShortcut"

        const val KEY_NAME = "name"
        const val KEY_SHORTCUT_NAME_OR_ID = "shortcut_id"

    }

}