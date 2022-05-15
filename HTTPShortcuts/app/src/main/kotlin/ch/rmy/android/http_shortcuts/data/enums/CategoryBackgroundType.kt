package ch.rmy.android.http_shortcuts.data.enums

import androidx.annotation.ColorInt

sealed interface CategoryBackgroundType {

    fun serialize(): String

    val useTextShadow: Boolean

    object Default : CategoryBackgroundType {
        const val VALUE = "default"

        override fun serialize() = VALUE

        override val useTextShadow: Boolean
            get() = false
    }

    object Wallpaper : CategoryBackgroundType {
        const val VALUE = "wallpaper"

        override fun serialize() = VALUE

        override val useTextShadow: Boolean
            get() = true
    }

    data class Color(@ColorInt val color: Int) : CategoryBackgroundType {
        override fun serialize(): String = "$PREFIX${getHexString()}"

        fun getHexString() =
            String.format("#%06x", color and 0xffffff)

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
        fun parse(type: String?): CategoryBackgroundType =
            when {
                type == Wallpaper.VALUE -> {
                    Wallpaper
                }
                type?.startsWith(Color.PREFIX) == true -> {
                    Color.deserialize(type) ?: Default
                }
                else -> Default
            }
    }
}
