package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class ChangeDescriptionActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = ChangeDescriptionAction(
        description = actionDTO.getString(KEY_DESCRIPTION) ?: "",
        shortcutNameOrId = actionDTO.getString(KEY_SHORTCUT_NAME_OR_ID)?.takeUnlessEmpty(),
    )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = listOf(KEY_SHORTCUT_NAME_OR_ID, KEY_DESCRIPTION),
    )

    companion object {

        const val TYPE = "change_description"
        const val FUNCTION_NAME = "changeDescription"

        const val KEY_DESCRIPTION = "description"
        const val KEY_SHORTCUT_NAME_OR_ID = "shortcut_id"
    }
}
