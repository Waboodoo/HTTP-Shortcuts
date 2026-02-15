package ch.rmy.android.http_shortcuts.data.enums

import androidx.compose.runtime.Stable

@Stable
enum class SyncTargetType(val value: String) {
    FILE("file"),
    URL("url"),
    ;

    companion object {
        fun parse(value: String) =
            entries.find { it.value == value }
    }
}
