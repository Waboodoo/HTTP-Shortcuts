package ch.rmy.android.http_shortcuts.activities.editor.mqttmessages.models

import androidx.compose.runtime.Stable

@Stable
data class MqttMessagesListItem(
    val id: Int,
    val topic: String,
    val payload: String,
)
