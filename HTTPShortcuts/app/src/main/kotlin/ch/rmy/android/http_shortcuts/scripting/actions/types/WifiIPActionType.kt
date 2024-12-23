package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import ch.rmy.android.scripting.JsFunctionArgs
import javax.inject.Inject

class WifiIPActionType
@Inject
constructor(
    private val wifiIPAction: WifiIPAction,
) : ActionType {
    override val type = TYPE

    override fun getActionRunnable(args: JsFunctionArgs) =
        ActionRunnable(
            action = wifiIPAction,
            params = Unit,
        )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = 0,
    )

    companion object {
        private const val TYPE = "wifi_ip"
        private const val FUNCTION_NAME = "getWifiIPAddress"
    }
}
