package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class SendUDPPacketAction(
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
                        "Failed to send UDP packet: $e"
                    }
                }
            }
        }
            .subscribeOn(Schedulers.io())

    companion object {
        private fun sendPacket(data: ByteArray, ipAddress: InetAddress, port: Int) {
            val packet = DatagramPacket(data, data.size, ipAddress, port)
            DatagramSocket().send(packet)
        }
    }
}
