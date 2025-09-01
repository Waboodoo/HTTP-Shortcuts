package ch.rmy.android.http_shortcuts.icons

import android.content.Context
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Stable
import androidx.core.net.toUri
import ch.rmy.android.http_shortcuts.R
import java.io.File

@Stable
sealed interface ShortcutIcon {
    @Stable
    data class BuiltInIcon(val iconName: String) : ShortcutIcon {
        override fun toString() = iconName

        override fun equals(other: Any?) =
            iconName == (other as? BuiltInIcon)?.iconName

        override fun hashCode() =
            iconName.hashCode()
    }

    @Stable
    data class ExternalResourceIcon(val uri: Uri) : ShortcutIcon {
        override fun toString() = uri.toString()

        override fun equals(other: Any?) =
            uri == (other as? ExternalResourceIcon)?.uri

        override fun hashCode() =
            uri.hashCode()
    }

    @Stable
    data class CustomIcon(val fileName: String) : ShortcutIcon {
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

    @Stable
    object NoIcon : ShortcutIcon {
        override fun toString() = ""

        val iconResource
            get() = R.drawable.ic_launcher

        override fun equals(other: Any?) =
            other is NoIcon

        override fun hashCode() =
            0
    }

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
