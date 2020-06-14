package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class WaitActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = WaitAction(actionDTO.data)

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        functionNameAliases = setOf(FUNCTION_NAME_ALIAS),
        parameters = listOf(WaitAction.KEY_DURATION)
    )

    companion object {

        const val TYPE = "wait"
        const val FUNCTION_NAME = "wait"
        const val FUNCTION_NAME_ALIAS = "sleep"

    }

}