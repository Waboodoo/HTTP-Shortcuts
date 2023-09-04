package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionData
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import javax.inject.Inject

class ToHexStringActionType
@Inject
constructor(
    private val toHexStringAction: ToHexStringAction,
) : ActionType {
    override val type = TYPE

    override fun getActionRunnable(actionDTO: ActionData) =
        ActionRunnable(
            action = toHexStringAction,
            params = ToHexStringAction.Params(
                data = actionDTO.getByteArray(0) ?: ByteArray(0),
            ),
        )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = 1,
    )

    companion object {
        private const val TYPE = "to_hex_string"
        private const val FUNCTION_NAME = "toHexString"
    }
}
