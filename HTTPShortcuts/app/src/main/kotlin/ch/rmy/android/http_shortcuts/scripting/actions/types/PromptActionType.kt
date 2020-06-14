package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class PromptActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = PromptAction(actionDTO.data)

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = listOf(PromptAction.KEY_MESSAGE, PromptAction.KEY_PREFILL)
    )

    companion object {

        const val TYPE = "prompt"
        const val FUNCTION_NAME = "prompt"

    }

}