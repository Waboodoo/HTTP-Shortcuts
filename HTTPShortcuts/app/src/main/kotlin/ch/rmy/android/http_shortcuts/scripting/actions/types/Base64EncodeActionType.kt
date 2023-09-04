package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionData
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import javax.inject.Inject

class Base64EncodeActionType
@Inject
constructor(
    private val base64EncodeAction: Base64EncodeAction,
) : ActionType {
    override val type = TYPE

    override fun getActionRunnable(actionDTO: ActionData) =
        ActionRunnable(
            action = base64EncodeAction,
            params = Base64EncodeAction.Params(
                text = actionDTO.getByteArray(0) ?: ByteArray(0),
            )
        )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        functionNameAliases = setOf("base64Encode", "btoa"),
        parameters = 1,
    )

    companion object {
        private const val TYPE = "base64encode"
        private const val FUNCTION_NAME = "base64encode"
    }
}
