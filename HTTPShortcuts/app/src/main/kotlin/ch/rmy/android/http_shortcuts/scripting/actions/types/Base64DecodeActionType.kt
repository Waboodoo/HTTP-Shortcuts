package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import ch.rmy.android.scripting.JsFunctionArgs
import javax.inject.Inject

class Base64DecodeActionType
@Inject
constructor(
    private val base64DecodeAction: Base64DecodeAction,
) : ActionType {
    override val type = TYPE

    override fun getActionRunnable(args: JsFunctionArgs) =
        ActionRunnable(
            action = base64DecodeAction,
            params = Base64DecodeAction.Params(
                encoded = args.getString(0) ?: "",
            ),
        )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        functionNameAliases = setOf("base64Decode", "atob"),
        parameters = 1,
    )

    companion object {
        private const val TYPE = "base64decode"
        private const val FUNCTION_NAME = "base64decode"
    }
}
