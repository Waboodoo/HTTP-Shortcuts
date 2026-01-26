package ch.rmy.android.http_shortcuts.icons

import androidx.annotation.ColorInt
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.utils.ColorUtil.colorIntToHexString
import ch.rmy.android.http_shortcuts.utils.ColorUtil.hexStringToColorInt
import java.time.Instant

@JvmInline
value class CustomIconName(val fileName: String) {

    override fun toString() = fileName

    val isCircular: Boolean
        get() = CIRCULAR_ICON_NAME in fileName

    val hasTransparency: Boolean
        get() = fileName.contains("$HAS_TRANSPARENCY_NAME(-[0-9A-Fa-f]{6})?".toRegex())

    @get:ColorInt
    val singleColor: Int?
        get() = "$HAS_TRANSPARENCY_NAME(-[0-9A-Fa-f]{6})?".toRegex()
            .find(fileName)
            ?.groupValues
            ?.get(1)
            ?.takeUnlessEmpty()
            ?.removePrefix("-")
            ?.hexStringToColorInt()

    companion object {
        fun parse(iconName: String): CustomIconName? =
            if (
                iconName.endsWith(".png", ignoreCase = true) ||
                iconName.endsWith(".jpg", ignoreCase = true)
            ) {
                CustomIconName(iconName)
            } else {
                null
            }

        fun generate(isCircular: Boolean, hasTransparency: Boolean, @ColorInt singleColor: Int?) =
            CustomIconName(
                buildString {
                    append(CUSTOM_ICON_NAME_PREFIX)
                    append(generatePrefix())
                    if (isCircular) {
                        append(CIRCULAR_ICON_NAME)
                    }
                    if (hasTransparency) {
                        append(HAS_TRANSPARENCY_NAME)
                        if (singleColor != null) {
                            append('-')
                            append(singleColor.colorIntToHexString())
                        }
                    }
                    append(".png")
                },
            )

        private fun generatePrefix() =
            (Instant.now().toEpochMilli() - 1767225600000L).toString()

        const val CUSTOM_ICON_NAME_PREFIX = "custom-icon_"
        private const val CIRCULAR_ICON_NAME = "_circle"
        private const val HAS_TRANSPARENCY_NAME = "_tr"
    }
}
