package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class PromptActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = PromptAction(
        message = actionDTO.getString(KEY_MESSAGE) ?: "",
        prefill = actionDTO.getString(KEY_PREFILL) ?: "",
    )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = listOf(KEY_MESSAGE, KEY_PREFILL),
    )

    companion object {

        const val TYPE = "prompt"
        const val FUNCTION_NAME = "prompt"

        const val KEY_MESSAGE = "message"
        const val KEY_PREFILL = "prefill"
    }
}
