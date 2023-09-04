package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionData
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import javax.inject.Inject

class WifiSSIDActionType
@Inject
constructor(
    private val wifiSSIDAction: WifiSSIDAction,
) : ActionType {
    override val type = TYPE

    override fun getActionRunnable(actionDTO: ActionData) =
        ActionRunnable(
            action = wifiSSIDAction,
            params = Unit,
        )

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
