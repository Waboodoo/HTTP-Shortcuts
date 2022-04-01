package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class WifiIPActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = WifiIPAction()

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = 0,
    )

    companion object {
        private const val TYPE = "wifi_ip"
        private const val FUNCTION_NAME = "getWifiIPAddress"
    }
}
