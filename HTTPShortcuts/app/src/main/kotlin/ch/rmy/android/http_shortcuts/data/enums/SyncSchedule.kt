package ch.rmy.android.http_shortcuts.data.enums

import androidx.compose.runtime.Stable

@Stable
enum class SyncSchedule(val value: String) {
    DAILY("daily"),
    WEEKLY("weekly"),
    ;

    companion object {
        fun parse(value: String) =
            entries.find { it.value == value }
    }
}
