package ch.rmy.android.http_shortcuts.history

import ch.rmy.android.framework.extensions.tryOrLog
import ch.rmy.android.http_shortcuts.data.domains.history.HistoryRepository
import ch.rmy.android.http_shortcuts.data.enums.HistoryEventType
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HistoryEventLogger
@Inject
constructor(
    private val historyRepository: HistoryRepository,
) {

    private val scope = CoroutineScope(Dispatchers.Default)

    fun logEvent(event: HistoryEvent) {
        scope.launch {
            tryOrLog {
                historyRepository.storeHistoryEvent(event.getType(), event)
            }
        }
    }

    private fun HistoryEvent.getType(): HistoryEventType =
        when (this) {
            is HistoryEvent.ShortcutTriggered -> HistoryEventType.SHORTCUT_TRIGGERED
            is HistoryEvent.ShortcutCancelled -> HistoryEventType.SHORTCUT_CANCELLED
            is HistoryEvent.HttpRequestSent -> HistoryEventType.HTTP_REQUEST_SENT
            is HistoryEvent.HttpResponseReceived -> HistoryEventType.HTTP_RESPONSE_RECEIVED
            is HistoryEvent.NetworkError -> HistoryEventType.NETWORK_ERROR
            is HistoryEvent.Error -> HistoryEventType.ERROR
            is HistoryEvent.CustomEvent -> HistoryEventType.CUSTOM_EVENT
        }
}
