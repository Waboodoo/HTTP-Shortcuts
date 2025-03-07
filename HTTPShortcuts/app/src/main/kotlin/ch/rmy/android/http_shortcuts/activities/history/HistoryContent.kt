package ch.rmy.android.http_shortcuts.activities.history

import android.text.format.DateUtils
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.collapse
import androidx.compose.ui.semantics.expand
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.EmptyState
import ch.rmy.android.http_shortcuts.components.FontSize
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.extensions.formatMediumTime
import ch.rmy.android.http_shortcuts.extensions.formatShortTime
import ch.rmy.android.http_shortcuts.extensions.localize
import java.time.Instant
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun HistoryContent(state: HistoryViewState, onLongPressed: (eventId: Int) -> Unit) {
    if (state.historyItems.isEmpty()) {
        EmptyState(
            title = stringResource(R.string.empty_state_history),
            description = stringResource(R.string.empty_state_history_instructions, stringResource(R.string.label_execution_settings)),
        )
        return
    }

    val expanded = remember {
        mutableStateMapOf<Int, Boolean>()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth(),
        content = {
            items(
                items = state.historyItems,
                key = { it.id },
            ) { historyItem ->
                val isExpanded = expanded.getOrDefault(historyItem.id, false)
                HistoryListItemView(
                    modifier = Modifier.semantics {
                        if (isExpanded) {
                            collapse {
                                consume {
                                    expanded[historyItem.id] = false
                                }
                            }
                        } else {
                            expand {
                                consume {
                                    expanded[historyItem.id] = true
                                }
                            }
                        }
                    },
                    historyItem = historyItem,
                    useRelativeTime = state.useRelativeTimes,
                    expanded = isExpanded,
                    onClick = {
                        expanded[historyItem.id] = !isExpanded
                    },
                    onLongPress = {
                        onLongPressed(historyItem.id)
                    },
                )
            }
        },
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryListItemView(
    modifier: Modifier,
    historyItem: HistoryListItem,
    useRelativeTime: Boolean,
    expanded: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
) {
    val now = if (useRelativeTime) now() else 0L

    Column(
        modifier = modifier
            .combinedClickable(
                onLongClick = {
                    onLongPress()
                },
                onClick = {
                    onClick()
                },
            ),
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = Spacing.MEDIUM, vertical = Spacing.SMALL)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(Spacing.SMALL),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.SMALL),
            ) {
                Title(
                    historyItem,
                    modifier = Modifier
                        .weight(1f, fill = true),
                )

                if (useRelativeTime) {
                    Time(time = relativeTime(historyItem.epochMillis, now))
                } else {
                    Time(time = historyItem.time.run { if (expanded) formatMediumTime() else formatShortTime() })
                }
            }

            if (expanded) {
                Detail(historyItem)
            }
        }
    }

    HorizontalDivider()
}

@Composable
private fun now(): Long {
    var now by remember {
        mutableLongStateOf(Instant.now().toEpochMilli())
    }
    LaunchedEffect(Unit) {
        while (isActive) {
            now = Instant.now().toEpochMilli()
            delay(1.seconds)
        }
    }
    return now
}

private fun relativeTime(epochMillis: Long, now: Long): String =
    DateUtils.getRelativeTimeSpanString(epochMillis, now, 0L, DateUtils.FORMAT_ABBREV_ALL)
        .toString()

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
        else -> MaterialTheme.colorScheme.onBackground
    }

@Composable
private fun Time(time: String) {
    Text(
        text = time,
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
