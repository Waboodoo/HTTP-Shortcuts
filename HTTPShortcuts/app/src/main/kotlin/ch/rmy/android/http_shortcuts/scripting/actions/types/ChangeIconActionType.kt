package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class ChangeIconActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = ChangeIconAction(
        shortcutNameOrId = actionDTO.getString(0)?.takeUnlessEmpty(),
        iconName = actionDTO.getString(1) ?: "",
    )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = 2,
    )

    companion object {
        private const val TYPE = "change_icon"
        private const val FUNCTION_NAME = "changeIcon"
    }
}
