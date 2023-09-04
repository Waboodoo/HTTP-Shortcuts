package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionData
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import javax.inject.Inject

class HmacActionType
@Inject
constructor(
    private val hmacAction: HmacAction,
) : ActionType {
    override val type = TYPE

    override fun getActionRunnable(actionDTO: ActionData) =
        ActionRunnable(
            action = hmacAction,
            params = HmacAction.Params(
                algorithm = actionDTO.getString(0) ?: "",
                key = actionDTO.getByteArray(1) ?: ByteArray(0),
                message = actionDTO.getByteArray(2) ?: ByteArray(0),
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
