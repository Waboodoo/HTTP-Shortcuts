package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlin.time.Duration.Companion.milliseconds

class WakeOnLanAction(
    private val macAddress: String,
    private val ipAddress: String,
    private val port: Int,
) : BaseAction() {

    override suspend fun execute(executionContext: ExecutionContext) {
        val macAddress = parseMacAddress(macAddress)
        withContext(Dispatchers.IO) {
            try {
                sendMagicPacket(
                    macAddress = macAddress,
                    ipAddress = InetAddress.getByName(ipAddress),
                    port = port,
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logException(e)
                throw ActionException { context ->
                    context.getString(R.string.error_action_type_send_wol_failed, e.message)
                }
            }
        }
    }

    companion object {

        private const val FF: Byte = 0xff.toByte()
        private const val RESEND_PACKET_COUNT = 3
        private val RESEND_DELAY = 350.milliseconds

        private suspend fun sendMagicPacket(macAddress: List<Byte>, ipAddress: InetAddress, port: Int) {
            val data = mutableListOf(FF, FF, FF, FF, FF, FF)
            for (i in 0 until 16) {
                data.addAll(macAddress)
            }

            val bytes = data.toByteArray()
            val packet = DatagramPacket(bytes, bytes.size, ipAddress, port)
            DatagramSocket()
                .use { socket ->
                    for (i in 0 until RESEND_PACKET_COUNT) {
                        if (i != 0) {
                            delay(RESEND_DELAY)
                        }
                        socket.send(packet)
                    }
                }
        }

        private fun parseMacAddress(macAddress: String): List<Byte> =
            macAddress.split(':', '-')
                .mapNotNull {
                    it
                        .takeIf { it.length <= 2 }
                        ?.toIntOrNull(16)
                        ?.toByte()
                }
                .takeIf { it.size == 6 }
                ?: throw ActionException { context ->
                    context.getString(R.string.error_action_type_send_wol_invalid_mac_address, macAddress)
                }
    }
}
