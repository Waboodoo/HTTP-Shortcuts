package ch.rmy.android.http_shortcuts.utils

import ch.rmy.android.framework.extensions.logException
import java.net.UnknownHostException
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage

class MqttUtil
@Inject
constructor() {
    suspend fun sendMessages(serverUri: String, username: String?, password: String?, messages: List<Message>) {
        withContext(Dispatchers.IO) {
            try {
                val client = MqttClient(serverUri, MqttClient.generateClientId(), null)
                val options = MqttConnectOptions().apply {
                    isCleanSession = true
                    if (username != null) {
                        this.userName = username
                    }
                    if (password != null) {
                        this.password = password.toCharArray()
                    }
                }
                client.connect(options)
                messages.forEach { message ->
                    client.publish(message.topic, MqttMessage(message.payload))
                }
                client.disconnect()
                client.close()
            } catch (e: MqttException) {
                val message = if (e.cause is UnknownHostException) {
                    "Could not find host at $serverUri"
                } else {
                    logException(e)
                    e.message
                        ?.takeUnless { it == "MqttException" }
                        ?: (e.cause?.toString())
                        ?: (e.toString().removePrefix("MqttException "))
                }
                throw MqttUtilException(message)
            } catch (e: IllegalArgumentException) {
                throw MqttUtilException(e.message)
            }
        }
    }

    data class Message(val topic: String, val payload: ByteArray) {
        override fun equals(other: Any?): Boolean {
            (other as? Message) ?: return false
            return topic == other.topic && payload.contentEquals(other.payload)
        }

        override fun hashCode(): Int =
            topic.hashCode() + payload.hashCode()
    }

    class MqttUtilException(override val message: String?) : Exception()

    companion object {
        private const val PREFIX = "###MESSAGE->"

        fun getMessagesFromBody(body: String): List<Message> =
            buildList {
                var topic: String? = null
                var payloadBuilder: StringBuilder? = null

                fun addMessage() {
                    add(Message(topic ?: return, payloadBuilder?.dropLast(1)?.toString()?.toByteArray() ?: return))
                }

                body.lines().forEach { line ->
                    if (line.startsWith(PREFIX)) {
                        addMessage()
                        topic = line.removePrefix(PREFIX)
                        payloadBuilder = StringBuilder()
                    } else {
                        payloadBuilder?.appendLine(line)
                    }
                }
                addMessage()
            }

        fun countMessagesInBody(body: String): Int =
            body.lines().count { it.startsWith(PREFIX) }

        fun getBodyFromMessages(messages: List<Message>): String =
            messages.joinToString("\n") { message ->
                "$PREFIX${message.topic}\n${String(message.payload)}"
            }
    }
}
