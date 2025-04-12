package ch.rmy.android.http_shortcuts.activities.editor.mqttmessages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.lifecycle.SavedStateHandle
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.FontSize
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.components.VariablePlaceholderTextField

@Composable
fun MqttMessagesDialogs(
    dialogState: MqttMessagesDialogState?,
    savedStateHandle: SavedStateHandle,
    onConfirmed: (topic: String, payload: String) -> Unit,
    onDelete: () -> Unit,
    onDismissed: () -> Unit,
) {
    when (dialogState) {
        is MqttMessagesDialogState.AddMessage -> {
            EditMqttMessageDialog(
                savedStateHandle = savedStateHandle,
                isEdit = false,
                onConfirmed = onConfirmed,
                onDismissed = onDismissed,
            )
        }
        is MqttMessagesDialogState.EditMessage -> {
            EditMqttMessageDialog(
                savedStateHandle = savedStateHandle,
                isEdit = true,
                initialTopic = dialogState.topic,
                initialPayload = dialogState.payload,
                onConfirmed = onConfirmed,
                onDelete = onDelete,
                onDismissed = onDismissed,
            )
        }
        null -> Unit
    }
}

@Composable
private fun EditMqttMessageDialog(
    savedStateHandle: SavedStateHandle,
    isEdit: Boolean,
    initialTopic: String = "",
    initialPayload: String = "",
    onConfirmed: (key: String, value: String) -> Unit,
    onDelete: () -> Unit = {},
    onDismissed: () -> Unit,
) {
    var topic by rememberSaveable(key = "edit-message-topic") {
        mutableStateOf(initialTopic)
    }
    var payload by rememberSaveable(key = "edit-message-payload") {
        mutableStateOf(initialPayload)
    }

    AlertDialog(
        onDismissRequest = onDismissed,
        title = {
            Text(stringResource(if (isEdit) R.string.title_mqtt_message_edit else R.string.title_mqtt_message_add))
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.SMALL),
            ) {
                VariablePlaceholderTextField(
                    savedStateHandle = savedStateHandle,
                    modifier = Modifier
                        .fillMaxWidth(),
                    key = "message-edit-topic",
                    value = topic,
                    label = {
                        Text(stringResource(R.string.label_mqtt_topic))
                    },
                    onValueChange = {
                        topic = it
                    },
                    textStyle = TextStyle(
                        fontSize = FontSize.SMALL,
                    ),
                    singleLine = true,
                )

                VariablePlaceholderTextField(
                    savedStateHandle = savedStateHandle,
                    modifier = Modifier
                        .fillMaxWidth(),
                    key = "message-edit-payload",
                    value = payload,
                    label = {
                        Text(stringResource(R.string.label_mqtt_payload))
                    },
                    onValueChange = {
                        payload = it
                    },
                    textStyle = TextStyle(
                        fontSize = FontSize.SMALL,
                    ),
                    maxLines = 6,
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = topic.isNotEmpty(),
                onClick = {
                    onConfirmed(topic, payload)
                },
            ) {
                Text(stringResource(R.string.dialog_ok))
            }
        },
        dismissButton = {
            if (isEdit) {
                TextButton(
                    onClick = onDelete,
                ) {
                    Text(stringResource(R.string.dialog_remove))
                }
            }
        },
    )
}
