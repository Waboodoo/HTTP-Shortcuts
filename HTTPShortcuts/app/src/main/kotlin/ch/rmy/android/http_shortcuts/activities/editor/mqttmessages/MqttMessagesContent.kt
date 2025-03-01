package ch.rmy.android.http_shortcuts.activities.editor.mqttmessages

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.rmy.android.framework.extensions.move
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.editor.mqttmessages.models.MqttMessagesListItem
import ch.rmy.android.http_shortcuts.components.EmptyState
import ch.rmy.android.http_shortcuts.components.VariablePlaceholderText
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MqttMessagesContent(
    messages: List<MqttMessagesListItem>,
    onMessageClicked: (Int) -> Unit,
    onMessageMoved: (Int, Int) -> Unit,
) {
    if (messages.isEmpty()) {
        EmptyState(
            title = stringResource(R.string.empty_state_mqtt_messages),
            description = stringResource(R.string.empty_state_mqtt_messages_instructions),
        )
        return
    }

    var localMessages by remember(messages) { mutableStateOf(messages) }
    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        localMessages = localMessages.move(from.index, to.index)
        onMessageMoved(from.key as Int, to.key as Int)
    }

    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxSize(),
    ) {
        items(
            items = localMessages,
            key = { it.id },
        ) { item ->
            ReorderableItem(reorderableState, key = item.id) { isDragging ->
                val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp)
                MessageItem(
                    message = item,
                    modifier = Modifier
                        .shadow(elevation.value)
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable {
                            onMessageClicked(item.id)
                        }
                        .longPressDraggableHandle(),
                )
            }
        }
    }
}

@Composable
private fun MessageItem(
    message: MqttMessagesListItem,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier,
    ) {
        ListItem(
            modifier = Modifier.fillMaxWidth(),
            headlineContent = {
                VariablePlaceholderText(message.topic, maxLines = 2, overflow = TextOverflow.Ellipsis)
            },
            supportingContent = {
                VariablePlaceholderText(message.payload.replace('\n', ' '), maxLines = 3, overflow = TextOverflow.Ellipsis)
            },
        )
        HorizontalDivider()
    }
}
