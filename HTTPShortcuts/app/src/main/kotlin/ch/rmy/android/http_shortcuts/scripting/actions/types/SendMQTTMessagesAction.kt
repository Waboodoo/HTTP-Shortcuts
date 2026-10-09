package ch.rmy.android.http_shortcuts.scripting.actions.types

import androidx.core.net.toUri
import ch.rmy.android.framework.extensions.tryOrIgnore
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.enums.HostVerificationConfig
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.LocalNetworkPermissionManager
import ch.rmy.android.http_shortcuts.utils.MqttUtil
import javax.inject.Inject

class SendMQTTMessagesAction
@Inject
constructor(
    private val mqttUtil: MqttUtil,
    private val localNetworkPermissionManager: LocalNetworkPermissionManager,
) : Action<SendMQTTMessagesAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext) {
        tryOrIgnore {
            serverUri.toUri().host
        }
            ?.let { host ->
                localNetworkPermissionManager.requestLocalNetworkPermissionIfNeeded(host)
            }

        try {
            mqttUtil.sendMessages(
                serverUri = serverUri,
                username = username,
                password = password,
                hostVerificationConfig = hostVerificationConfig,
                messages = messages.map { MqttUtil.Message(it.topic, it.payload) },
            )
        } catch (e: MqttUtil.MqttUtilException) {
            throw ActionException {
                getString(R.string.error_failed_to_send_mqtt, e.message)
            }
        }
    }

    data class Message(val topic: String, val payload: ByteArray)

    data class Params(
        val serverUri: String,
        val username: String?,
        val password: String?,
        val hostVerificationConfig: HostVerificationConfig,
        val messages: List<Message>,
    )
}
