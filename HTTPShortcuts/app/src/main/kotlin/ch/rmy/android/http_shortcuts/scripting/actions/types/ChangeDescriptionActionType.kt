package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionData
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import javax.inject.Inject

class ChangeDescriptionActionType
@Inject
constructor(
    private val changeDescriptionAction: ChangeDescriptionAction,
) : ActionType {
    override val type = TYPE

    override fun getActionRunnable(actionDTO: ActionData) =
        ActionRunnable(
            action = changeDescriptionAction,
            params = ChangeDescriptionAction.Params(
                shortcutNameOrId = actionDTO.getString(0)?.takeUnlessEmpty(),
                description = actionDTO.getString(1) ?: "",
            )
        )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = 2,
    )

    companion object {
        private const val TYPE = "change_description"
        private const val FUNCTION_NAME = "changeDescription"
    }
}
