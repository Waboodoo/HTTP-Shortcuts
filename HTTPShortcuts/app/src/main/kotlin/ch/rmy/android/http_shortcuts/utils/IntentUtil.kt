package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.data.dtos.LauncherShortcut
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

object IntentUtil {

    private const val ACTION_INSTALL_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT"
    private const val ACTION_UNINSTALL_SHORTCUT = "com.android.launcher.action.UNINSTALL_SHORTCUT"
    private const val EXTRA_SHORTCUT_DUPLICATE = "duplicate"

    fun getShortcutId(intent: Intent): String =
        intent.getStringExtra(ExecuteActivity.EXTRA_SHORTCUT_ID)
            ?: intent.data?.lastPathSegment
            ?: ""

    fun getVariableValues(intent: Intent): Map<String, String> {
        val serializable = intent.getSerializableExtra(ExecuteActivity.EXTRA_VARIABLE_VALUES)
        if (serializable is Map<*, *>) {
            @Suppress("UNCHECKED_CAST")
            return serializable as Map<String, String>
        }
        return emptyMap()
    }

    @Suppress("DEPRECATION")
    fun getLegacyShortcutPlacementIntent(context: Context, shortcut: LauncherShortcut, install: Boolean): Intent {
        val shortcutIntent = ExecuteActivity.IntentBuilder(shortcut.id)
            .build(context)
        val addIntent = Intent()
            .putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
            .putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcut.name)
            .putExtra(EXTRA_SHORTCUT_DUPLICATE, true)

        try {
            val iconUri = shortcut.icon.getIconURI(context, external = true)
            val scaledIcon = MediaStore.Images.Media.getBitmap(context.contentResolver, iconUri)
            val size = IconUtil.getIconSize(context, scaled = false)
            val unscaledIcon = Bitmap.createScaledBitmap(scaledIcon, size, size, false)
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, unscaledIcon)
        } catch (e: Exception) {
            addIntent.putExtra(
                Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(context.applicationContext, ShortcutIcon.NoIcon.ICON_RESOURCE),
            )
        }

        addIntent.action = if (install) ACTION_INSTALL_SHORTCUT else ACTION_UNINSTALL_SHORTCUT

        return addIntent
    }
}
