package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionData
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import javax.inject.Inject

class SendUDPPacketActionType
@Inject
constructor(
    private val sendUDPPacketAction: SendUDPPacketAction,
) : ActionType {
    override val type = TYPE

    override fun getActionRunnable(actionDTO: ActionData) =
        ActionRunnable(
            action = sendUDPPacketAction,
            params = SendUDPPacketAction.Params(
                data = actionDTO.getByteArray(0) ?: ByteArray(0),
                ipAddress = actionDTO.getString(1)?.takeUnlessEmpty() ?: "255.255.255.255",
                port = actionDTO.getInt(2) ?: 0,
            ),
        )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        functionNameAliases = setOf(FUNCTION_NAME_ALIAS),
        parameters = 3,
    )

    companion object {
        private const val TYPE = "send_udp_packet"
        private const val FUNCTION_NAME = "sendUDPPacket"
        private const val FUNCTION_NAME_ALIAS = "sendUdpPacket"
    }
}
