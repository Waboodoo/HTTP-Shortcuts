package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class SendUDPPacketAction(
    private val data: ByteArray,
    private val ipAddress: String,
    private val port: Int,
) : BaseAction() {

    override suspend fun execute(executionContext: ExecutionContext) {
        withContext(Dispatchers.IO) {
            try {
                sendPacket(
                    data = data,
                    ipAddress = InetAddress.getByName(ipAddress),
                    port = port,
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logException(e)
                throw ActionException {
                    getString(R.string.error_failed_to_send_udp, e.message ?: e.toString())
                }
            }
        }
    }

    companion object {
        internal fun sendPacket(data: ByteArray, ipAddress: InetAddress, port: Int) {
            val packet = DatagramPacket(data, data.size, ipAddress, port)
            DatagramSocket().send(packet)
        }
    }
}
