package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import java.io.StringWriter
import java.net.InetAddress
import java.net.Socket
import java.nio.charset.Charset
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.closeQuietly

class SendTCPPacketAction
@Inject
constructor() : Action<SendTCPPacketAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext): String? =
        withContext(Dispatchers.IO) {
            try {
                Socket(InetAddress.getByName(ipAddress), port).use { socket ->
                    executionContext.cleanupHandler.doFinally {
                        socket.closeQuietly()
                    }

                    with(socket.getOutputStream()) {
                        write(data)
                        flush()
                    }

                    val readMode = options["read"]
                        ?: return@withContext null

                    var timeoutReached = false
                    val timeoutMonitor = launch {
                        delay(options.getTimeout())
                        timeoutReached = true
                        socket.closeQuietly()
                    }
                    val writer = StringWriter()
                    try {
                        when (readMode) {
                            "text" -> {
                                socket.getInputStream()
                                    .reader(options.getCharset())
                                    .copyTo(writer)
                                writer.toString()
                            }
                            "line" -> {
                                socket.getInputStream()
                                    .reader(options.getCharset())
                                    .buffered()
                                    .readLine()
                            }
                            else -> throw ActionException {
                                "Unknown read mode: $readMode"
                            }
                        }
                    } catch (e: Exception) {
                        if (timeoutReached) {
                            writer.toString()
                        } else {
                            logException(e)
                            throw ActionException {
                                getString(R.string.error_failed_to_receive_tcp, e.message ?: e.toString())
                            }
                        }
                    } finally {
                        timeoutMonitor.cancel()
                    }
                }
            } catch (e: ActionException) {
                throw e
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logException(e)
                throw ActionException {
                    getString(R.string.error_failed_to_send_tcp, e.message ?: e.toString())
                }
            }
        }

    private fun Map<String, Any?>.getCharset() =
        get("charset")
            ?.let { it as? String }
            ?.takeIf(Charset::isSupported)
            ?.let(Charset::forName)
            ?: Charsets.UTF_8

    private fun Map<String, Any?>.getTimeout(): Long =
        get("timeout")
            ?.let { (it as? Long) ?: (it as? Int)?.toLong() }
            ?.takeIf { it in 1L..60_000L }
            ?: 3000L

    data class Params(
        val data: ByteArray,
        val ipAddress: String,
        val port: Int,
        val options: Map<String, Any?>,
    )
}
