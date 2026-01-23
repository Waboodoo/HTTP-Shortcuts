package ch.rmy.android.http_shortcuts.icons

import java.time.Instant

@JvmInline
value class CustomIconName(val fileName: String) {

    override fun toString() = fileName

    val isCircular: Boolean
        get() = CUSTOM_CIRCULAR_ICON_NAME_SUFFIX in fileName

    val hasTransparency: Boolean
        get() = fileName.substringBeforeLast('.').endsWith(CUSTOM_HAS_TRANSPARENCY_NAME_SUFFIX)

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

        fun generate(isCircular: Boolean, hasTransparency: Boolean) =
            CustomIconName(
                CUSTOM_ICON_NAME_PREFIX +
                    generatePrefix() +
                    (if (isCircular) CUSTOM_CIRCULAR_ICON_NAME_SUFFIX else "") +
                    (if (hasTransparency) CUSTOM_HAS_TRANSPARENCY_NAME_SUFFIX else "") +
                    ".png",
            )

        private fun generatePrefix() =
            (Instant.now().toEpochMilli() - 1388448000000L).toString()

        const val CUSTOM_ICON_NAME_PREFIX = "custom-icon_"
        private const val CUSTOM_CIRCULAR_ICON_NAME_SUFFIX = "_circle"
        private const val CUSTOM_HAS_TRANSPARENCY_NAME_SUFFIX = "_tr"
    }
}
