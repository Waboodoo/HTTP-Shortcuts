package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class HmacActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = HmacAction(
        algorithm = actionDTO.getString(KEY_ALGORITHM) ?: "",
        key = actionDTO.getString(KEY_KEY) ?: "",
        message = actionDTO.getString(KEY_MESSAGE) ?: "",
    )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = listOf(KEY_ALGORITHM, KEY_KEY, KEY_MESSAGE),
    )

    companion object {

        const val TYPE = "hmac"
        const val FUNCTION_NAME = "hmac"

        const val KEY_ALGORITHM = "algorithm"
        const val KEY_KEY = "key"
        const val KEY_MESSAGE = "message"

    }

}
