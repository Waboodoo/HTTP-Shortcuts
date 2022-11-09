package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.Socket

class SendTCPPacketAction(
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
                    getString(R.string.error_failed_to_send_tcp, e.message ?: e.toString())
                }
            }
        }
    }

    companion object {
        private fun sendPacket(data: ByteArray, ipAddress: InetAddress, port: Int) {
            Socket(ipAddress, port).use { socket ->
                with(socket.getOutputStream()) {
                    write(data)
                    flush()
                }
            }
        }
    }
}
