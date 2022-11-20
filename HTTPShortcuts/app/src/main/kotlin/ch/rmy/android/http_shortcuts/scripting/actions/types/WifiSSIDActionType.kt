package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class WifiSSIDActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = WifiSSIDAction()

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = 1,
        functionNameAliases = setOf("getWifiSsid"),
    )

    companion object {
        private const val TYPE = "wifi_ssid"
        private const val FUNCTION_NAME = "getWifiSSID"
    }
}
