package ch.rmy.android.http_shortcuts.activities.history

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.EmptyState
import ch.rmy.android.http_shortcuts.components.FontSize
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.extensions.formatMediumTime
import ch.rmy.android.http_shortcuts.extensions.formatShortTime
import ch.rmy.android.http_shortcuts.extensions.localize

@Composable
fun HistoryContent(state: HistoryViewState, onLongPressed: (eventId: String) -> Unit) {
    if (state.historyItems.isEmpty()) {
        EmptyState(
            title = stringResource(R.string.empty_state_history),
            description = stringResource(R.string.empty_state_history_instructions, stringResource(R.string.label_execution_settings)),
        )
        return
    }

    val expanded = remember {
        mutableStateMapOf<String, Boolean>()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth(),
        content = {
            items(
                items = state.historyItems,
                key = { it.id },
            ) { historyItem ->
                HistoryListItemView(
                    historyItem,
                    expanded = expanded.getOrDefault(historyItem.id, false),
                    onClick = {
                        expanded[historyItem.id] = !expanded.getOrDefault(historyItem.id, false)
                    },
                    onLongPress = {
                        onLongPressed(historyItem.id)
                    },
                )
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryListItemView(
    historyItem: HistoryListItem,
    expanded: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
) {
    Column(
        modifier = Modifier
            .combinedClickable(
                onLongClick = {
                    onLongPress()
                },
                onClick = {
                    onClick()
                },
            )
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = Spacing.MEDIUM, vertical = Spacing.SMALL)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(Spacing.SMALL)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.SMALL),
            ) {
                Title(
                    historyItem,
                    modifier = Modifier
                        .weight(1f, fill = true)
                )

                Time(historyItem, detailed = expanded)
            }

            if (expanded) {
                Detail(historyItem)
            }
        }
    }

    Divider()
}

@Composable
private fun Title(historyItem: HistoryListItem, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = historyItem.title.localize(),
        fontSize = FontSize.SMALL,
        fontWeight = FontWeight.SemiBold,
        color = historyItem.getTitleColor(),
    )
}

@Composable
private fun HistoryListItem.getTitleColor(): Color =
    when (displayType) {
        HistoryListItem.DisplayType.SUCCESS -> colorResource(R.color.history_text_color_success)
        HistoryListItem.DisplayType.FAILURE -> colorResource(R.color.history_text_color_failure)
        else -> colorResource(R.color.text_color_primary_dark)
    }

@Composable
private fun Time(historyItem: HistoryListItem, detailed: Boolean) {
    Text(
        text = historyItem.time.run { if (detailed) formatMediumTime() else formatShortTime() },
        fontSize = FontSize.TINY,
    )
}

@Composable
private fun Detail(historyItem: HistoryListItem) {
    historyItem.detail?.localize()?.let { detail ->
        Text(
            text = detail,
            fontSize = FontSize.SMALL,
        )
    }
}
