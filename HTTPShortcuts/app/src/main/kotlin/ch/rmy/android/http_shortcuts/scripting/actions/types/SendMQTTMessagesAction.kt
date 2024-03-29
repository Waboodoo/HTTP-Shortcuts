package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.applyIfNotNull
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import javax.inject.Inject

class SendMQTTMessagesAction
@Inject
constructor() : Action<SendMQTTMessagesAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext) {
        withContext(Dispatchers.IO) {
            try {
                val client = MqttClient(serverUri, MqttClient.generateClientId(), null)
                val options = MqttConnectOptions()
                    .apply {
                        isCleanSession = true
                    }
                    .applyIfNotNull(username) {
                        userName = it
                    }
                    .applyIfNotNull(password) {
                        password = it.toCharArray()
                    }
                client.connect(options)
                messages.forEach { message ->
                    client.publish(message.topic, MqttMessage(message.payload))
                }
                client.disconnect()
                client.close()
            } catch (e: MqttException) {
                logException(e)
                throw ActionException {
                    val message = e.message
                        ?.takeUnless { it == "MqttException" }
                        ?: (e.toString().removePrefix("MqttException "))
                    getString(R.string.error_failed_to_send_mqtt, message)
                }
            } catch (e: IllegalArgumentException) {
                throw ActionException {
                    getString(R.string.error_failed_to_send_mqtt, e.message)
                }
            }
        }
    }

    data class Message(val topic: String, val payload: ByteArray)

    data class Params(
        val serverUri: String,
        val username: String?,
        val password: String?,
        val messages: List<Message>,
    )
}
