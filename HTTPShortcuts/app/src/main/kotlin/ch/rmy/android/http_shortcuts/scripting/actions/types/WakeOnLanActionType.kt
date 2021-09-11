package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class WakeOnLanActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = WakeOnLanAction(
        macAddress = actionDTO[KEY_MAC_ADDRESS] ?: "",
        ipAddress = actionDTO[KEY_IP_ADDRESS]?.takeUnlessEmpty() ?: "255.255.255.255",
        port = actionDTO[KEY_PORT]?.toIntOrNull() ?: 9,
    )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        functionNameAliases = setOf(FUNCTION_NAME_ALIAS),
        parameters = listOf(KEY_MAC_ADDRESS),
    )

    companion object {

        const val TYPE = "wake_on_lan"
        const val FUNCTION_NAME = "wakeOnLan"
        const val FUNCTION_NAME_ALIAS = "wakeOnLAN"

        const val KEY_MAC_ADDRESS = "mac_address"
        const val KEY_IP_ADDRESS = "ip_address"
        const val KEY_PORT = "port"
    }

}
