package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import ch.rmy.android.scripting.JsFunctionArgs
import javax.inject.Inject

class SendTCPPacketActionType
@Inject
constructor(
    private val sendTCPPacketAction: SendTCPPacketAction,
) : ActionType {
    override val type = TYPE

    override fun getActionRunnable(args: JsFunctionArgs) =
        ActionRunnable(
            action = sendTCPPacketAction,
            params = SendTCPPacketAction.Params(
                data = args.getByteArray(0) ?: ByteArray(0),
                ipAddress = args.getString(1)?.takeUnlessEmpty() ?: "255.255.255.255",
                port = args.getInt(2) ?: 0,
                options = args.getObject(3) ?: emptyMap(),
            ),
        )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        functionNameAliases = setOf(FUNCTION_NAME_ALIAS),
        parameters = 4,
    )

    companion object {
        private const val TYPE = "send_tcp_packet"
        private const val FUNCTION_NAME = "sendTCPPacket"
        private const val FUNCTION_NAME_ALIAS = "sendTcpPacket"
    }
}
