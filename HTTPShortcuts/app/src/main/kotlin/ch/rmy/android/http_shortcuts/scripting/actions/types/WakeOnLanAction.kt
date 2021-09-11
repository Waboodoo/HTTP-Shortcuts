package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.extensions.logException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress


class WakeOnLanAction(
    private val macAddress: String,
    private val ipAddress: String,
    private val port: Int,
) : BaseAction() {

    override fun execute(executionContext: ExecutionContext): Completable =
        Completable.fromAction {
            try {
                sendMagicPacket(
                    macAddress = parseMacAddress(macAddress),
                    ipAddress = InetAddress.getByName(ipAddress),
                    port = port,
                )
            } catch (e: Exception) {
                logException(e)
                throw ActionException {
                    "Failed to send Wake-on-LAN packet: ${e.message}"
                }
            }
        }
            .subscribeOn(Schedulers.io())

    companion object {

        private const val FF: Byte = 0xff.toByte()
        private const val RESEND_PACKET_COUNT = 3
        private const val RESEND_DELAY = 350L

        private fun sendMagicPacket(macAddress: List<Byte>, ipAddress: InetAddress, port: Int) {

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
                            Thread.sleep(RESEND_DELAY)
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
                ?: throw ActionException {
                    "Invalid MAC address: $macAddress"
                }
    }
}
