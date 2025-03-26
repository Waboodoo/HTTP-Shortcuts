package ch.rmy.android.http_shortcuts.data.enums

import androidx.compose.runtime.Stable

@Stable
enum class ResponseSuccessOutput(val type: String) {
    RESPONSE("response"),
    MESSAGE("message"),
    NONE("none"),
    ;

    companion object {
        fun parse(value: String): ResponseSuccessOutput? =
            entries.find { it.type == value }
    }
}
