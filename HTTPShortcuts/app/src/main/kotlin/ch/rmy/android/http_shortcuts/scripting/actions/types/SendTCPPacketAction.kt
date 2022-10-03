package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import java.net.InetAddress
import java.net.Socket


class SendTCPPacketAction(
    private val data: ByteArray,
    private val ipAddress: String,
    private val port: Int,
) : BaseAction() {

    override fun execute(executionContext: ExecutionContext): Completable =
        Completable.fromAction {
            try {
                sendPacket(
                    data = data,
                    ipAddress = InetAddress.getByName(ipAddress),
                    port = port,
                )
            } catch (e: Exception) {
                if (e is ActionException) {
                    throw e
                } else {
                    logException(e)
                    throw ActionException {
                        "Failed to send TCP packet: $e"
                    }
                }
            }
        }
            .subscribeOn(Schedulers.io())

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
