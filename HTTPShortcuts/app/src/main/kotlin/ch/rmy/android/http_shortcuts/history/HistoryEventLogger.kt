package ch.rmy.android.http_shortcuts.history

import ch.rmy.android.http_shortcuts.data.domains.history.HistoryRepository
import ch.rmy.android.http_shortcuts.data.enums.HistoryEventType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class HistoryEventLogger
@Inject
constructor(
    private val historyRepository: HistoryRepository,
) {

    private val scope = CoroutineScope(Dispatchers.IO)

    fun logEvent(event: HistoryEvent) {
        scope.launch {
            historyRepository.storeHistoryEvent(event.getType(), event)
        }
    }

    private fun HistoryEvent.getType(): HistoryEventType =
        when (this) {
            is HistoryEvent.ShortcutTriggered -> HistoryEventType.SHORTCUT_TRIGGERED
            is HistoryEvent.HttpRequestSent -> HistoryEventType.HTTP_REQUEST_SENT
            is HistoryEvent.HttpResponseReceived -> HistoryEventType.HTTP_RESPONSE_RECEIVED
            is HistoryEvent.NetworkError -> HistoryEventType.NETWORK_ERROR
            is HistoryEvent.Error -> HistoryEventType.ERROR
        }
}
