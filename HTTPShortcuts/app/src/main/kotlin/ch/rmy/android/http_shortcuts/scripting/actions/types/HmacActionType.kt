package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class HmacActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = HmacAction(
        algorithm = actionDTO.getString(0) ?: "",
        key = actionDTO.getByteArray(1) ?: ByteArray(0),
        message = actionDTO.getString(2) ?: "",
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
