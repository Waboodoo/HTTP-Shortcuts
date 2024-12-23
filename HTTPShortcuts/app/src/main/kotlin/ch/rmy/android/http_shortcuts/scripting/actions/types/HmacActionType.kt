package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import ch.rmy.android.scripting.JsFunctionArgs
import javax.inject.Inject

class HmacActionType
@Inject
constructor(
    private val hmacAction: HmacAction,
) : ActionType {
    override val type = TYPE

    override fun getActionRunnable(args: JsFunctionArgs) =
        ActionRunnable(
            action = hmacAction,
            params = HmacAction.Params(
                algorithm = args.getString(0) ?: "",
                key = args.getByteArray(1) ?: ByteArray(0),
                message = args.getByteArray(2) ?: ByteArray(0),
            ),
        )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = 3,
    )

    companion object {
        private const val TYPE = "hmac"
        private const val FUNCTION_NAME = "hmac"
    }
}
