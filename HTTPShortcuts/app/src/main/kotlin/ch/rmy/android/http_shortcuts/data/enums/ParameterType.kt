package ch.rmy.android.http_shortcuts.data.enums

import androidx.compose.runtime.Stable

@Stable
enum class ParameterType(val type: String) {
    STRING("string"),
    FILE("file"),
    ;

    override fun toString() =
        type

    companion object {
        fun parse(type: String): ParameterType? =
            entries.find { it.type == type }
    }
}
