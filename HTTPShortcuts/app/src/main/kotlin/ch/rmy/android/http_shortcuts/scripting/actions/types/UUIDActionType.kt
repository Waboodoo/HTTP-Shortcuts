package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class UUIDActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) =
        UUIDAction()

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = 0,
    )

    companion object {
        private const val TYPE = "uuidv4"
        private const val FUNCTION_NAME = "uuidv4"
    }
}
