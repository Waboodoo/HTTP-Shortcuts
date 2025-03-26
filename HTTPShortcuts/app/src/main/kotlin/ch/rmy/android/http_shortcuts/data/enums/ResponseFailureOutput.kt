package ch.rmy.android.http_shortcuts.data.enums

import androidx.compose.runtime.Stable

@Stable
enum class ResponseFailureOutput(val type: String) {
    DETAILED("detailed"),
    SIMPLE("simple"),
    NONE("none"),
    ;

    companion object {
        fun parse(value: String): ResponseFailureOutput? =
            entries.find { it.type == value }
    }
}
