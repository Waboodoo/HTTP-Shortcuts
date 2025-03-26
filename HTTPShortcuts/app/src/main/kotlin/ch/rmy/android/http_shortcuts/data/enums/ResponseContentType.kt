package ch.rmy.android.http_shortcuts.data.enums

import androidx.compose.runtime.Stable

@Stable
enum class ResponseContentType(val key: String) {
    PLAIN_TEXT("plain_text"),
    JSON("json"),
    XML("xml"),
    HTML("html"),
    ;

    companion object {
        fun parse(key: String): ResponseContentType? =
            entries.find { it.key == key }
    }
}
