package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class OpenAppActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = OpenAppAction(
        packageName = actionDTO.getString(KEY_PACKAGE_NAME) ?: "",
    )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = listOf(KEY_PACKAGE_NAME),
    )

    companion object {

        const val TYPE = "open_app"
        const val FUNCTION_NAME = "openApp"

        const val KEY_PACKAGE_NAME = "packageName"
    }
}
