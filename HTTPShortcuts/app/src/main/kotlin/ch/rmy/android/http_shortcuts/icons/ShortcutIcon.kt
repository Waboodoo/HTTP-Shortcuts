package ch.rmy.android.http_shortcuts.icons

import android.content.Context
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.core.net.toUri
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.extensions.mapFor
import ch.rmy.android.http_shortcuts.extensions.mapIf
import ch.rmy.android.http_shortcuts.extensions.replacePrefix
import ch.rmy.android.http_shortcuts.utils.IconUtil
import java.io.File

sealed class ShortcutIcon {
    data class BuiltInIcon(val iconName: String) : ShortcutIcon() {
        override fun getIconURI(context: Context, external: Boolean): Uri =
            if (external) {
                Uri.fromFile(IconUtil.generateRasterizedIconForBuiltInIcon(context, this))
            } else {
                val identifier = getDrawableIdentifier(context)
                getDrawableUri(context, identifier)
            }

        val tint: Int?
            get() =
                Icons.TintColors.values().firstOrNull {
                    iconName.startsWith(it.prefix)
                }
                    ?.color

        @DrawableRes
        fun getDrawableIdentifier(context: Context): Int =
            context.resources.getIdentifier(
                normalizedIconName,
                "drawable",
                context.packageName,
            )
                .takeUnless { it == 0 }
                ?: NoIcon.ICON_RESOURCE

        private val normalizedIconName: String =
            iconName
                .mapFor(Icons.TintColors.values().asIterable()) { tintColor ->
                    mapIf(startsWith(tintColor.prefix)) {
                        replacePrefix(tintColor.prefix, Icons.DEFAULT_TINT_PREFIX)
                    }
                }

        override fun toString() = iconName

        override fun equals(other: Any?) =
            iconName == (other as? BuiltInIcon)?.iconName

        override fun hashCode() =
            iconName.hashCode()

        companion object {
            fun fromDrawableResource(
                context: Context,
                @DrawableRes resource: Int,
                tint: Icons.TintColors? = null,
            ): BuiltInIcon {
                val iconName = context.resources.getResourceEntryName(resource)
                    .mapIf(tint != null) {
                        replacePrefix(Icons.DEFAULT_TINT_PREFIX, tint!!.prefix)
                    }
                return BuiltInIcon(iconName)
            }
        }
    }

    data class ExternalResourceIcon(val uri: Uri) : ShortcutIcon() {
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

    data class CustomIcon(val fileName: String) : ShortcutIcon() {
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

    object NoIcon : ShortcutIcon() {
        override fun toString() = ""

        override fun getIconURI(context: Context, external: Boolean): Uri =
            getDrawableUri(context, ICON_RESOURCE)

        const val ICON_RESOURCE = R.drawable.ic_launcher

        override fun equals(other: Any?) =
            other is NoIcon

        override fun hashCode() =
            0
    }

    abstract fun getIconURI(context: Context, external: Boolean = false): Uri

    companion object {
        fun fromName(iconName: String?): ShortcutIcon =
            when {
                iconName == null -> NoIcon
                iconName.startsWith("android.resource://") -> ExternalResourceIcon(iconName.toUri())
                iconName.endsWith(".png") -> CustomIcon(iconName)
                else -> BuiltInIcon(iconName)
            }

        private fun getDrawableUri(context: Context, @DrawableRes identifier: Int): Uri =
            Uri.parse("android.resource://${context.packageName}/$identifier")
    }
}