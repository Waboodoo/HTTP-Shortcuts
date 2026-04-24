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
        get() = "$HAS_TRANSPARENCY_NAME(-[0-9A-Fa-f]{6})".toRegex()
            .find(iconName)
            ?.groupValues
            ?.get(1)
            ?.takeUnlessEmpty()
            ?.removePrefix("-")
            ?.hexStringToColorInt()

    @get:ColorInt
    val tint: Int?
        get() = iconName.toUri()
            .getQueryParameter(PARAM_TINT)
            ?.hexStringToColorInt()

    companion object {

        fun fromCanonical(canonicalIconName: String, @ColorInt tint: Int) =
            CustomIconName("$canonicalIconName?$PARAM_TINT=${tint.colorIntToHexString()}")

        fun parse(iconName: String): CustomIconName? =
            if (iconName.matches(".+\\.(png|jpg)(\\?.+)?$".toRegex(RegexOption.IGNORE_CASE))) {
                CustomIconName(iconName)
            } else {
                null
            }

        fun generate(
            prefix: String? = null,
            isCircular: Boolean,
            hasTransparency: Boolean,
            @ColorInt singleColor: Int?,
            @ColorInt tint: Int? = null,
        ) =
            CustomIconName(
                buildString {
                    append(CUSTOM_ICON_NAME_PREFIX)
                    append(prefix ?: generatePrefix())
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
                    if (tint != null) {
                        append("?$PARAM_TINT=")
                        append(tint.colorIntToHexString())
                    }
                },
            )

        private fun generatePrefix() =
            (Instant.now().toEpochMilli() - 1767225600000L).toString()

        const val CUSTOM_ICON_NAME_PREFIX = "custom-icon_"
        private const val CIRCULAR_ICON_NAME = "_circle"
        private const val HAS_TRANSPARENCY_NAME = "_tr"

        private const val PARAM_TINT = "tint"
    }
}
