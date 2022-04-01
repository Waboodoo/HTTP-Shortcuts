package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class WakeOnLanActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = WakeOnLanAction(
        macAddress = actionDTO.getString(0) ?: "",
        ipAddress = actionDTO.getString(1)?.takeUnlessEmpty() ?: "255.255.255.255",
        port = actionDTO.getInt(2) ?: 9,
    )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        functionNameAliases = setOf(FUNCTION_NAME_ALIAS),
        parameters = 3,
    )

    companion object {
        private const val TYPE = "wake_on_lan"
        private const val FUNCTION_NAME = "wakeOnLan"
        private const val FUNCTION_NAME_ALIAS = "wakeOnLAN"
    }
}
