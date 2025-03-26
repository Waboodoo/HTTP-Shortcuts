package ch.rmy.android.http_shortcuts.data.enums

enum class PendingExecutionType {
    UNKNOWN,
    INITIAL_DELAY,
    RETRY_LATER,
    REPEAT,
    EXPLICITLY_SCHEDULED,
    NEW_INTENT,
    ;

    companion object {
        fun parse(name: String): PendingExecutionType? =
            entries.find { it.name == name }
    }
}
