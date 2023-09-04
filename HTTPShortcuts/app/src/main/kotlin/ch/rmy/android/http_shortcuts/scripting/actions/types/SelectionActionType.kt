package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionData
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import javax.inject.Inject

class SelectionActionType
@Inject
constructor(
    private val selectionAction: SelectionAction,
) : ActionType {
    override val type = TYPE

    override fun getActionRunnable(actionDTO: ActionData) =
        ActionRunnable(
            action = selectionAction,
            params = SelectionAction.Params(
                dataObject = actionDTO.getObject(0),
                dataList = actionDTO.getList(0),
            ),
        )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = 1,
    )

    companion object {
        private const val TYPE = "show_selection"
        private const val FUNCTION_NAME = "showSelection"
    }
}
