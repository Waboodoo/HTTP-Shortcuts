package ch.rmy.android.http_shortcuts.data.enums

import androidx.compose.runtime.Stable

@Stable
enum class ResponseDisplayAction(val key: String) {
    RERUN("rerun"),
    SHARE("share"),
    COPY("copy"),
    SAVE("save"),
    ;

    companion object {
        fun parse(key: String): ResponseDisplayAction? =
            entries.find { it.key == key }
    }
}
