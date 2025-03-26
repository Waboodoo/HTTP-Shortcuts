package ch.rmy.android.http_shortcuts.icons

import android.content.Context
import android.net.Uri
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Stable
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

@Stable
sealed interface ShortcutIcon {
    @Stable
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
                ?: Icons.TintColor.entries
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

        val hasBackground: Boolean
            get() = iconName.startsWith("flat_") ||
                iconName in ICONS_WITH_BACKGROUND

        val isUsableAsSilhouette
            get() = iconName.run {
                startsWith("bitsies_") || startsWith("black_") ||
                    (
                        startsWith("freepik_") && this !in arrayOf(
                            "freepik_accept",
                            "freepik_add",
                            "freepik_minus",
                            "freepik_cancel",
                            "freepik_heart",
                            "freepik_rate",
                        )
                        )
            }

        @DrawableRes
        fun getDrawableIdentifier(context: Context): Int =
            context.resources.getIdentifier(
                normalizedIconName,
                "drawable",
                context.packageName,
            )
                .takeUnless { it == 0 }
                ?: NoIcon.iconResource

        val normalizedIconName: String = run {
            iconName
                .run {
                    COLOR_SUFFIX_REGEX.matchEntire(this)
                        ?.let { matchResult ->
                            matchResult.groupValues[1]
                        }
                        ?: this
                }
                .runFor(Icons.TintColor.entries.asIterable()) { tintColor ->
                    runIf(startsWith(tintColor.prefix)) {
                        replacePrefix(tintColor.prefix, Icons.DEFAULT_TINT_PREFIX)
                    }
                }
                .run {
                    if (startsWith(LEGACY_COLOR_PREFIX)) {
                        "black_circle"
                    } else {
                        this
                    }
                }
        }

        val plainName: String by lazy(LazyThreadSafetyMode.NONE) {
            var name = normalizedIconName
            Icons.PREFIXES.forEach { prefix ->
                name = name.removePrefix(prefix)
            }
            name.filter { it.isLetter() || it == '_' }
                .trimEnd('_')
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
                tint: Icons.TintColor? = null,
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

            private val ICONS_WITH_BACKGROUND = arrayOf(
                "freepik_modem",
                "freepik_print",
                "freepik_tv",
                "freepik_projector",
                "freepik_check",
                "freepik_close",
                "freepik_accept",
                "freepik_add",
                "freepik_minus",
                "freepik_cancel",
                "freepik_heart",
                "freepik_rate",
            )
        }
    }

    @Stable
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

    @Stable
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

        override val isCircular: Boolean
            get() = fileName.contains(IconUtil.CUSTOM_CIRCULAR_ICON_NAME_SUFFIX)

        override fun toString() = fileName

        override fun equals(other: Any?) =
            fileName == (other as? CustomIcon)?.fileName

        override fun hashCode() =
            fileName.hashCode()
    }

    @Stable
    object NoIcon : ShortcutIcon {
        override fun toString() = ""

        override fun getIconURI(context: Context, external: Boolean): Uri =
            getDrawableUri(context, iconResource)

        val iconResource
            get() = R.drawable.ic_launcher

        override fun equals(other: Any?) =
            other is NoIcon

        override fun hashCode() =
            0
    }

    fun getIconURI(context: Context, external: Boolean = false): Uri

    val isCircular: Boolean
        get() = false

    companion object {
        fun fromName(iconName: String?): ShortcutIcon =
            when {
                iconName.isNullOrEmpty() -> NoIcon
                iconName.startsWith("android.resource://") -> ExternalResourceIcon(iconName.toUri())
                iconName.endsWith(".png", ignoreCase = true) || iconName.endsWith(".jpg", ignoreCase = true) -> CustomIcon(iconName)
                else -> BuiltInIcon(iconName)
            }

        internal fun getDrawableUri(context: Context, @DrawableRes identifier: Int): Uri =
            "android.resource://${context.packageName}/$identifier".toUri()
    }
}
