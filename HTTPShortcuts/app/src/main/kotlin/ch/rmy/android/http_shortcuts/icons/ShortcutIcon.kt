package ch.rmy.android.http_shortcuts.icons

import android.content.Context
import android.net.Uri
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.net.toUri
import ch.rmy.android.framework.extensions.replacePrefix
import ch.rmy.android.framework.extensions.runFor
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.utils.ColorUtil.colorIntToHexString
import ch.rmy.android.http_shortcuts.utils.ColorUtil.hexStringToColorInt
import ch.rmy.android.http_shortcuts.utils.IconUtil
import java.io.File

sealed interface ShortcutIcon {
    data class BuiltInIcon(val iconName: String) : ShortcutIcon {
        override fun getIconURI(context: Context, external: Boolean): Uri =
            if (external) {
                Uri.fromFile(IconUtil.generateRasterizedIconForBuiltInIcon(context, this))
            } else {
                val identifier = getDrawableIdentifier(context)
                getDrawableUri(context, identifier)
            }

        val tint: Int? = run {
            COLOR_SUFFIX_REGEX.matchEntire(iconName)
                ?.let { matchResult ->
                    matchResult.groupValues[2]
                        .hexStringToColorInt()
                }
                ?: Icons.TintColors.values()
                    .firstOrNull {
                        iconName.startsWith(it.prefix)
                    }
                    ?.color
                ?: iconName.takeIf { it.startsWith(LEGACY_COLOR_PREFIX) }
                    ?.let {
                        LEGACY_COLORS[it.removePrefix(LEGACY_COLOR_PREFIX)]
                            ?.plus(0xff000000.toInt())
                    }
        }

        @DrawableRes
        fun getDrawableIdentifier(context: Context): Int =
            context.resources.getIdentifier(
                normalizedIconName,
                "drawable",
                context.packageName,
            )
                .takeUnless { it == 0 }
                ?: NoIcon.ICON_RESOURCE

        private val normalizedIconName: String = run {
            iconName
                .run {
                    COLOR_SUFFIX_REGEX.matchEntire(this)
                        ?.let { matchResult ->
                            matchResult.groupValues[1]
                        }
                        ?: this
                }
                .runFor(Icons.TintColors.values().asIterable()) { tintColor ->
                    runIf(startsWith(tintColor.prefix)) {
                        replacePrefix(tintColor.prefix, Icons.DEFAULT_TINT_PREFIX)
                    }
                }
                .run {
                    if (startsWith(LEGACY_COLOR_PREFIX)) {
                        "black_circle"
                    } else this
                }
        }

        override fun toString() = iconName

        override fun equals(other: Any?) =
            iconName == (other as? BuiltInIcon)?.iconName

        override fun hashCode() =
            iconName.hashCode()

        fun withTint(@ColorInt tint: Int): BuiltInIcon =
            BuiltInIcon("${normalizedIconName}_${tint.colorIntToHexString()}")

        companion object {
            fun fromDrawableResource(
                context: Context,
                @DrawableRes resource: Int,
                tint: Icons.TintColors? = null,
            ): BuiltInIcon {
                val iconName = context.resources.getResourceEntryName(resource)
                    .runIfNotNull(tint) {
                        plus("_${it.color.colorIntToHexString()}")
                    }
                return BuiltInIcon(iconName)
            }

            private val COLOR_SUFFIX_REGEX = "^(.+)_([A-F0-9]{6})$".toRegex(RegexOption.IGNORE_CASE)

            private const val LEGACY_COLOR_PREFIX = "circle_"

            private val LEGACY_COLORS = mapOf(
                "black" to 0x000000,
                "blue" to 0x0067FF,
                "blue_dark" to 0x002980,
                "cyan" to 0x00FAFD,
                "green" to 0x64FF00,
                "green_dark" to 0x246800,
                "orange" to 0xFF9C00,
                "brown" to 0x724B1E,
                "magenta" to 0xFF00F3,
                "purple" to 0x9600FD,
                "red" to 0xCC0000,
                "yellow" to 0xFFFD00,
                "white" to 0xFFFFFF,
                "grey" to 0x888888,
            )
        }
    }

    data class ExternalResourceIcon(val uri: Uri) : ShortcutIcon {
        override fun getIconURI(context: Context, external: Boolean) = uri

        val packageName: String
            get() = uri.pathSegments[0]

        @get:DrawableRes
        val resourceId: Int
            get() = uri.pathSegments[1].toInt()

        override fun toString() = uri.toString()

        override fun equals(other: Any?) =
            uri == (other as? ExternalResourceIcon)?.uri

        override fun hashCode() =
            uri.hashCode()
    }

    data class CustomIcon(val fileName: String) : ShortcutIcon {
        override fun getIconURI(context: Context, external: Boolean): Uri =
            getFile(context)?.let(Uri::fromFile)
                ?: NoIcon.getIconURI(context, external)

        fun getFile(context: Context): File? =
            try {
                context.getFileStreamPath(fileName)
            } catch (e: Exception) {
                null
            }

        override fun toString() = fileName

        override fun equals(other: Any?) =
            fileName == (other as? CustomIcon)?.fileName

        override fun hashCode() =
            fileName.hashCode()
    }

    object NoIcon : ShortcutIcon {
        override fun toString() = ""

        override fun getIconURI(context: Context, external: Boolean): Uri =
            getDrawableUri(context, ICON_RESOURCE)

        const val ICON_RESOURCE = R.drawable.ic_launcher

        override fun equals(other: Any?) =
            other is NoIcon

        override fun hashCode() =
            0
    }

    fun getIconURI(context: Context, external: Boolean = false): Uri

    companion object {
        fun fromName(iconName: String?): ShortcutIcon =
            when {
                iconName == null -> NoIcon
                iconName.startsWith("android.resource://") -> ExternalResourceIcon(iconName.toUri())
                iconName.endsWith(".png", ignoreCase = true) || iconName.endsWith(".jpg", ignoreCase = true) -> CustomIcon(iconName)
                else -> BuiltInIcon(iconName)
            }

        private fun getDrawableUri(context: Context, @DrawableRes identifier: Int): Uri =
            "android.resource://${context.packageName}/$identifier".toUri()
    }
}
