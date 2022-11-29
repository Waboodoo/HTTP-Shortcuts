package ch.rmy.android.http_shortcuts.activities.history

import ch.rmy.android.framework.viewmodel.viewstate.DialogState

data class HistoryViewState(
    val dialogState: DialogState? = null,
    val historyItems: List<HistoryListItem>,
) {
    val isClearButtonVisible: Boolean
        get() = historyItems.singleOrNull() != HistoryListItem.EmptyState
}
