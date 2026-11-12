package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import ch.rmy.android.scripting.JsFunctionArgs
import javax.inject.Inject

class OpenCategoryActionType
@Inject
constructor(
    private val openCategoryAction: OpenCategoryAction,
) : ActionType {
    override val type = TYPE

    override fun getActionRunnable(args: JsFunctionArgs) =
        ActionRunnable(
            action = openCategoryAction,
            params = OpenCategoryAction.Params(
                categoryNameOrId = args.getString(0) ?: "",
            ),
        )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = 1,
    )

    companion object {
        private const val TYPE = "open_category"
        private const val FUNCTION_NAME = "openCategory"
    }
}
