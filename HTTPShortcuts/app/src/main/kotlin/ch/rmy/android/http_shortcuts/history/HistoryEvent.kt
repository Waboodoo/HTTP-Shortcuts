package ch.rmy.android.http_shortcuts.history

import android.net.Uri
import androidx.annotation.Keep
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType

sealed interface HistoryEvent {
    @Keep
    data class ShortcutTriggered(
        val shortcutName: String,
        val trigger: ShortcutTriggerType?,
    ) : HistoryEvent

    @Keep
    data class ShortcutCancelled(
        val shortcutName: String,
    ) : HistoryEvent

    @Keep
    data class HttpRequestSent(
        val shortcutName: String,
        val url: Uri,
        val method: String,
        val headers: Map<String, List<String>>,
    ) : HistoryEvent

    @Keep
    data class HttpResponseReceived(
        val shortcutName: String,
        val responseCode: Int,
        val isSuccess: Boolean,
        val headers: Map<String, List<String>>,
    ) : HistoryEvent

    @Keep
    data class NetworkError(
        val shortcutName: String,
        val error: String,
    ) : HistoryEvent

    @Keep
    data class Error(
        val shortcutName: String,
        val error: String,
    ) : HistoryEvent

    @Keep
    data class CustomEvent(
        val title: String,
        val message: String?,
    ) : HistoryEvent

    @Keep
    data class SyncImportSucceed(
        val importedShortcuts: Int,
    ) : HistoryEvent

    @Keep
    data class SyncImportFailed(
        val details: String? = null,
    ) : HistoryEvent

    @Keep
    data class SyncExportSucceed(
        val exportedShortcuts: Int,
    ) : HistoryEvent

    @Keep
    data class SyncExportFailed(
        val details: String? = null,
    ) : HistoryEvent
}
