package ch.rmy.android.http_shortcuts.data.enums

import androidx.compose.runtime.Stable

@Stable
enum class ResponseUiType(val type: String) {
    TOAST("toast"),
    NOTIFICATION("notification"),
    DIALOG("dialog"),
    WINDOW("window"),
    ;

    override fun toString() =
        type

    companion object {
        fun parse(type: String): ResponseUiType? =
            entries.find { it.type == type }
    }
}
