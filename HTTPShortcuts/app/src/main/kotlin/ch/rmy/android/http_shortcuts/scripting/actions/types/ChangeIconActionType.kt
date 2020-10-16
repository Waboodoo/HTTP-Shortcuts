package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class ChangeIconActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = ChangeIconAction(
        icon = actionDTO[KEY_ICON] ?: "",
        shortcutNameOrId = actionDTO[KEY_SHORTCUT_NAME_OR_ID]?.takeUnlessEmpty(),
    )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = listOf(KEY_SHORTCUT_NAME_OR_ID, KEY_ICON),
    )

    companion object {

        const val TYPE = "change_icon"
        const val FUNCTION_NAME = "changeIcon"

        const val KEY_ICON = "icon"
        const val KEY_SHORTCUT_NAME_OR_ID = "shortcut_id"

    }

}