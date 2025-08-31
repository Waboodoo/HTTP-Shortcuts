package ch.rmy.android.http_shortcuts.data.enums

import androidx.compose.runtime.Stable

@Stable
enum class SyncType {
    IMPORT,
    EXPORT,
    ;

    companion object {
        fun parse(value: String) =
            entries.find { it.name == value }
    }
}
