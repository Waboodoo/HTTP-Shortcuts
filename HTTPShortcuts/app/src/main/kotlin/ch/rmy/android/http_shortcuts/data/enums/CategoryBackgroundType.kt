package ch.rmy.android.http_shortcuts.data.enums

import androidx.annotation.ColorInt
import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.utils.ColorUtil.colorIntToHexString

@Stable
sealed interface CategoryBackgroundType {

    fun serialize(): String

    val useTextShadow: Boolean

    data object Default : CategoryBackgroundType {
        const val VALUE = "default"

        override fun serialize() = VALUE

        override val useTextShadow: Boolean
            get() = false
    }

    data class Color(@ColorInt val color: Int) : CategoryBackgroundType {
        override fun serialize(): String = "$PREFIX${getHexString()}"

        fun getHexString() =
            "#${color.colorIntToHexString()}"

        override val useTextShadow: Boolean
            get() = true

        override fun toString() =
            serialize()

        companion object {
            const val PREFIX = "color="

            fun deserialize(input: String): Color? =
                input.removePrefix(PREFIX)
                    .drop(1)
                    .toIntOrNull(16)
                    ?.plus(0xff000000.toInt())
                    ?.let(::Color)
        }
    }

    companion object {
        fun parse(type: String): CategoryBackgroundType? =
            when {
                type.startsWith(Color.PREFIX) -> {
                    Color.deserialize(type)
                }
                type == Default.VALUE -> Default
                else -> null
            }
    }
}
