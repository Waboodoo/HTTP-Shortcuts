package ch.rmy.android.http_shortcuts.data.enums

import androidx.annotation.ColorInt
import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.utils.ColorUtil.colorIntWithAlphaToHexString

@Stable
sealed interface WidgetBackgroundType {

    fun serialize(): String

    data class Color(@ColorInt val color: Int) : WidgetBackgroundType {
        override fun serialize(): String = "$PREFIX${color.colorIntWithAlphaToHexString()}"

        fun getHexString() =
            "#${color.colorIntWithAlphaToHexString()}"

        override fun toString() =
            serialize()

        companion object {
            const val PREFIX = "color="

            fun deserialize(input: String): Color? =
                input.removePrefix(PREFIX)
                    .toUIntOrNull(16)
                    ?.toInt()
                    ?.let(::Color)
        }
    }

    companion object {
        fun parse(type: String): WidgetBackgroundType? =
            when {
                type.startsWith(Color.PREFIX) -> {
                    Color.deserialize(type)
                }
                else -> null
            }
    }
}
