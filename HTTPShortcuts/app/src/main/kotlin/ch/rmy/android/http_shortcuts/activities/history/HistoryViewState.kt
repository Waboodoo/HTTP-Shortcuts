package ch.rmy.android.http_shortcuts.activities.history

import androidx.compose.runtime.Stable

@Stable
data class HistoryViewState(
    val historyItems: List<HistoryListItem>,
) {
    val isClearButtonEnabled: Boolean
        get() = historyItems.isNotEmpty()
}
