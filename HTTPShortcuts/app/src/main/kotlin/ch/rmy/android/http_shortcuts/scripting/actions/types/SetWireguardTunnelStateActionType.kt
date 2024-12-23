package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import ch.rmy.android.scripting.JsFunctionArgs
import javax.inject.Inject

class SetWireguardTunnelStateActionType
@Inject
constructor(
    private val setWireguardTunnelStateAction: SetWireguardTunnelStateAction,
) : ActionType {
    override val type = TYPE

    override fun getActionRunnable(args: JsFunctionArgs) =
        ActionRunnable(
            action = setWireguardTunnelStateAction,
            params = SetWireguardTunnelStateAction.Params(
                tunnel = args.getString(0) ?: "",
                state = args.getBoolean(1) != false,
            ),
        )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = 2,
    )

    companion object {
        private const val TYPE = "set_wireguard_tunnel_state"
        private const val FUNCTION_NAME = "setWireguardTunnelState"
    }
}
