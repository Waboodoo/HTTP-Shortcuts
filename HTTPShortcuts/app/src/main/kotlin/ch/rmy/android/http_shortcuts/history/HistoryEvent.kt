package ch.rmy.android.http_shortcuts.history

import android.net.Uri
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType

sealed interface HistoryEvent {
    data class ShortcutTriggered(
        val shortcutName: String,
        val trigger: ShortcutTriggerType?,
    ) : HistoryEvent

    data class ShortcutCancelled(
        val shortcutName: String,
    ) : HistoryEvent

    data class HttpRequestSent(
        val shortcutName: String,
        val url: Uri,
        val method: String,
        val headers: Map<String, List<String>>,
    ) : HistoryEvent

    data class HttpResponseReceived(
        val shortcutName: String,
        val responseCode: Int,
        val isSuccess: Boolean,
        val headers: Map<String, List<String>>,
    ) : HistoryEvent

    data class NetworkError(
        val shortcutName: String,
        val error: String,
    ) : HistoryEvent

    data class Error(
        val shortcutName: String,
        val error: String,
    ) : HistoryEvent

    data class CustomEvent(
        val title: String,
        val message: String?,
    ) : HistoryEvent
}
