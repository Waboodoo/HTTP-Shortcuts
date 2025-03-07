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
        val options = args.getObject(1) ?: emptyMap()
        return ActionRunnable(
            action = sendHttpRequestAction,
            params = SendHttpRequestAction.Params(
                url = args.getString(0) ?: "",
                method = options["method"]?.toString()?.uppercase() ?: "GET",
                body = options["body"]?.toString(),
                headers = (options["headers"] as? Map<*, *>)
                    ?.toStringMap(),
                formData = (options["formData"] as? Map<*, *>)
                    ?.toStringMap(),
                charsetOverride = options["charset"] as? String,
            ),
        )
    }

    private fun Map<*, *>.toStringMap(): Map<String, String> =
        mapKeys { (key, _) -> key.toString() }
            .mapValues { (_, value) -> value.toString() }

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
