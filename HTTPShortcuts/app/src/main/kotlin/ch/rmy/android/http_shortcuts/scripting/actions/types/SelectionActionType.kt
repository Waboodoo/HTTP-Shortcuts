package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class SelectionActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = SelectionAction(
        dataObject = actionDTO.getObject(KEY_DATA),
        dataList = actionDTO.getList(KEY_DATA),
    )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = listOf(KEY_DATA),
    )

    companion object {

        const val TYPE = "show_selection"
        const val FUNCTION_NAME = "showSelection"

        const val KEY_DATA = "data"

    }

}