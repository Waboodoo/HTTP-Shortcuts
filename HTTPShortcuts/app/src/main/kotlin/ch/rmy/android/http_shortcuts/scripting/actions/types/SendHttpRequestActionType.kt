package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import ch.rmy.android.scripting.JsFunctionArgs
import javax.inject.Inject

class SendHttpRequestActionType
@Inject
constructor(
    private val sendHttpRequestAction: SendHttpRequestAction,
) : ActionType {
    override val type = TYPE

    override fun getActionRunnable(args: JsFunctionArgs): ActionRunnable<*> {
        val options = args.getObject(1)
        return ActionRunnable(
            action = sendHttpRequestAction,
            params = SendHttpRequestAction.Params(
                url = args.getString(0) ?: "",
                method = (options?.get("method") as? String)?.uppercase() ?: "GET",
            ),
        )
    }

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        functionNameAliases = setOf("sendHTTPRequest"),
        parameters = 2,
    )

    companion object {
        private const val TYPE = "send_http_request"
        private const val FUNCTION_NAME = "sendHttpRequest"
    }
}
