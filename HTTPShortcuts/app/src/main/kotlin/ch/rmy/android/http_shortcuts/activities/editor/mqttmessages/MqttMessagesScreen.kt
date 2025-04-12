package ch.rmy.android.http_shortcuts.activities.editor.mqttmessages

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.SavedStateHandle
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.FloatingAddButton
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.bindViewModel

@Composable
fun MqttMessagesScreen(
    savedStateHandle: SavedStateHandle,
) {
    val (viewModel, state) = bindViewModel<MqttMessagesViewState, MqttMessagesViewModel>()

    BackHandler(state != null) {
        viewModel.onBackPressed()
    }

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.section_mqtt_messages),
        floatingActionButton = {
            FloatingAddButton(
                onClick = viewModel::onAddMessageButtonClicked,
                contentDescription = stringResource(R.string.accessibility_label_add_mqtt_message_fab),
            )
        },
    ) { viewState ->
        MqttMessagesContent(
            messages = viewState.messageItems,
            onMessageClicked = viewModel::onMessageClicked,
            onMessageMoved = viewModel::onMessageMoved,
        )
    }

    MqttMessagesDialogs(
        dialogState = state?.dialogState,
        savedStateHandle = savedStateHandle,
        onConfirmed = viewModel::onDialogConfirmed,
        onDelete = viewModel::onRemoveMessageButtonClicked,
        onDismissed = viewModel::onDismissDialog,
    )
}
