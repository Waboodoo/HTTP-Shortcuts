package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import ch.rmy.android.scripting.JsFunctionArgs
import javax.inject.Inject

class DialogActionType
@Inject
constructor(
    private val dialogAction: DialogAction,
) : ActionType {
    override val type = TYPE

    override fun getActionRunnable(args: JsFunctionArgs) =
        ActionRunnable(
            action = dialogAction,
            params = DialogAction.Params(
                message = args.getString(0) ?: "",
                title = args.getString(1) ?: "",
            ),
        )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        functionNameAliases = setOf(FUNCTION_NAME_ALIAS),
        parameters = 2,
    )

    companion object {
        private const val TYPE = "show_dialog"
        private const val FUNCTION_NAME = "showDialog"
        private const val FUNCTION_NAME_ALIAS = "alert"
    }
}
