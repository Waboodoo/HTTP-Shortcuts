package ch.rmy.android.http_shortcuts.activities.editor.mqttmessages

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.activities.editor.mqttmessages.models.MqttMessagesListItem

@Stable
data class MqttMessagesViewState(
    val dialogState: MqttMessagesDialogState? = null,
    val messageItems: List<MqttMessagesListItem> = emptyList(),
)
