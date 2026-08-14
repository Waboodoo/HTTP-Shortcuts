package ch.rmy.android.http_shortcuts.data.enums

import androidx.compose.runtime.Stable

@Stable
enum class CategoryAlignment(val value: String) {
    TOP("top"),
    CENTER("center"),
    BOTTOM("bottom"),
    ;

    override fun toString() =
        value

    companion object {
        fun parse(value: String): CategoryAlignment? =
            entries.find { it.value == value }
    }
}
