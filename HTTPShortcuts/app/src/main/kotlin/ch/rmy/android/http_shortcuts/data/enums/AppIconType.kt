package ch.rmy.android.http_shortcuts.data.enums

import androidx.compose.runtime.Stable

@Stable
enum class AppIconType(val type: String) {
    DEFAULT(""),
    BLACK_AND_WHITE("black-white"),
    WHITE_AND_BLACK("white-black"),
    SOLARIZED("solarized"),
    HACKER("hacker"),
    GREEN("green"),
    ;

    override fun toString() =
        type

    companion object {
        fun parse(type: String): AppIconType? =
            entries.find { it.type == type }
    }
}
