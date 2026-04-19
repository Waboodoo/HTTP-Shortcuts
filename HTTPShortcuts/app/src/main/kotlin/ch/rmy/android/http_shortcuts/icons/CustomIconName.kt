package ch.rmy.android.http_shortcuts.icons

import androidx.annotation.ColorInt
import androidx.core.net.toUri
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.utils.ColorUtil.colorIntToHexString
import ch.rmy.android.http_shortcuts.utils.ColorUtil.hexStringToColorInt
import java.time.Instant

@JvmInline
value class CustomIconName(val iconName: String) {
    val fileName: String
        get() = iconName.takeWhile { it != '?' }

    override fun toString() = iconName

    val isCircular: Boolean
        get() = CIRCULAR_ICON_NAME in iconName

    val hasTransparency: Boolean
        get() = iconName.contains("$HAS_TRANSPARENCY_NAME(-[0-9A-Fa-f]{6})?\\.".toRegex())

    @get:ColorInt
    val singleColor: Int?
        get() {
            return iconName.toUri().getQueryParameter("color")
                ?.hexStringToColorInt()
                ?: run {
                    // Legacy way of storing color information: as part of the file name itself
                    "$HAS_TRANSPARENCY_NAME(-[0-9A-Fa-f]{6})?".toRegex()
                        .find(iconName)
                        ?.groupValues
                        ?.get(1)
                        ?.takeUnlessEmpty()
                        ?.removePrefix("-")
                        ?.hexStringToColorInt()
                }
        }

    companion object {
        fun parse(iconName: String): CustomIconName? =
            if (iconName.matches(".+\\.(png|jpg|svg)(\\?.+)?$".toRegex(RegexOption.IGNORE_CASE))) {
                CustomIconName(iconName)
            } else {
                null
            }

        fun generate(prefix: String? = null, isCircular: Boolean, hasTransparency: Boolean, @ColorInt singleColor: Int?) =
            CustomIconName(
                buildString {
                    append(CUSTOM_ICON_NAME_PREFIX)
                    append(prefix ?: generatePrefix())
                    if (isCircular) {
                        append(CIRCULAR_ICON_NAME)
                    }
                    if (hasTransparency) {
                        append(HAS_TRANSPARENCY_NAME)
                    }
                    append(".png")
                    if (singleColor != null) {
                        append("?color=")
                        append(singleColor.colorIntToHexString())
                    }
                },
            )

        private fun generatePrefix() =
            (Instant.now().toEpochMilli() - 1767225600000L).toString()

        const val CUSTOM_ICON_NAME_PREFIX = "custom-icon_"
        private const val CIRCULAR_ICON_NAME = "_circle"
        private const val HAS_TRANSPARENCY_NAME = "_tr"
    }
}
