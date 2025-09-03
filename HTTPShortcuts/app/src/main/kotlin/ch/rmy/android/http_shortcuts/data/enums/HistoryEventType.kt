package ch.rmy.android.http_shortcuts.data.enums

enum class HistoryEventType(
    val type: String,
) {
    SHORTCUT_TRIGGERED("shortcut_triggered"),
    SHORTCUT_CANCELLED("shortcut_cancelled"),
    HTTP_REQUEST_SENT("http_request_sent"),
    HTTP_RESPONSE_RECEIVED("http_response_received"),
    NETWORK_ERROR("network_error"),
    ERROR("error"),
    CUSTOM_EVENT("custom_event"),
    SYNC_IMPORT_SUCCESS("sync_import_success"),
    SYNC_IMPORT_FAILED("sync_import_failed"),
    SYNC_EXPORT_SUCCESS("sync_export_success"),
    SYNC_EXPORT_FAILED("sync_export_failed"),
    ;

    override fun toString() =
        type

    companion object {
        fun parse(type: String): HistoryEventType? =
            entries.find { it.type == type }
    }
}
