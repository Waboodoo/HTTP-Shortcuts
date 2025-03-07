package ch.rmy.android.http_shortcuts.utils

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class WakeOnLanUtil
@Inject
constructor() {
    suspend fun send(macAddress: String, ipAddress: String?, port: Int?) {
        val macAddress = parseMacAddress(macAddress)
        withContext(Dispatchers.IO) {
            sendMagicPacket(
                macAddress = macAddress,
                ipAddress = InetAddress.getByName(ipAddress ?: "255.255.255.255"),
                port = port ?: 9,
            )
        }
    }

    private suspend fun sendMagicPacket(macAddress: List<Byte>, ipAddress: InetAddress, port: Int) {
        val bytes = buildList<Byte> {
            repeat(6) {
                add(FF)
            }
            repeat(16) {
                addAll(macAddress)
            }
        }
            .toByteArray()

        val packet = DatagramPacket(bytes, bytes.size, ipAddress, port)
        DatagramSocket()
            .use { socket ->
                repeat(RESEND_PACKET_COUNT) { i ->
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
            ?: throw InvalidMACAddressException()

    class InvalidMACAddressException : IllegalArgumentException()

    companion object {
        private const val FF: Byte = 0xff.toByte()
        private const val RESEND_PACKET_COUNT = 3
        private val RESEND_DELAY = 350.milliseconds
    }
}
