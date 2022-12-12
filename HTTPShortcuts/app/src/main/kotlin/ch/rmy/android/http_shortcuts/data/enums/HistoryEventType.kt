package ch.rmy.android.http_shortcuts.data.enums

enum class HistoryEventType(
    val type: String,
) {
    SHORTCUT_TRIGGERED("shortcut_triggered"),
    HTTP_REQUEST_SENT("http_request_sent"),
    HTTP_RESPONSE_RECEIVED("http_response_received"),
    NETWORK_ERROR("network_error"),
    ERROR("error");

    override fun toString() =
        type

    companion object {
        fun parse(type: String?) =
            values().firstOrNull { it.type == type }
    }
}
