package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.applyIfNotNull
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage

class SendMQTTMessagesAction(
    private val serverUri: String,
    private val username: String?,
    private val password: String?,
    private val messages: List<Message>,
) : BaseAction() {

    override fun execute(executionContext: ExecutionContext): Completable =
        Completable.fromAction {
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
                    "Failed to send MQTT message: $e"
                }
            }
        }
            .subscribeOn(Schedulers.io())

    data class Message(val topic: String, val payload: ByteArray)
}
