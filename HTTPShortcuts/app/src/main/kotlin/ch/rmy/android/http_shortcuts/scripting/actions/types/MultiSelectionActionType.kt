package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import ch.rmy.android.scripting.JsFunctionArgs
import javax.inject.Inject

class MultiSelectionActionType
@Inject
constructor(
    private val multiSelectionAction: MultiSelectionAction,
) : ActionType {
    override val type = TYPE

    override fun getActionRunnable(args: JsFunctionArgs) =
        ActionRunnable(
            action = multiSelectionAction,
            params = MultiSelectionAction.Params(
                dataObject = args.getObject(0),
                dataList = args.getListOfStrings(0),
                title = args.getString(1)?.takeUnlessEmpty(),
            ),
        )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = 2,
    )

    companion object {
        private const val TYPE = "show_multi_selection"
        private const val FUNCTION_NAME = "showMultiSelection"
    }
}
