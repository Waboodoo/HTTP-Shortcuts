package ch.rmy.android.http_shortcuts.activities.editor.mqttmessages

import androidx.compose.runtime.Stable

@Stable
sealed class MqttMessagesDialogState {
    @Stable
    data object AddMessage : MqttMessagesDialogState()

    @Stable
    data class EditMessage(
        val id: Int,
        val topic: String,
        val payload: String,
    ) : MqttMessagesDialogState()
}
