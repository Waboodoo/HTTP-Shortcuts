package ch.rmy.android.http_shortcuts.data.enums

import androidx.compose.runtime.Stable

@Stable
enum class SyncType(val value: String) {
    IMPORT("import"),
    EXPORT("export"),
    ;

    companion object {
        fun parse(value: String) =
            entries.find { it.value == value }
    }
}
