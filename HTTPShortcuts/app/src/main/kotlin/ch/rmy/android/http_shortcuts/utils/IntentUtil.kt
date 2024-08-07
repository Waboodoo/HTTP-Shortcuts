package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import ch.rmy.android.framework.extensions.createIntent
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.data.dtos.ShortcutPlaceholder
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

object IntentUtil {

    private const val ACTION_INSTALL_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT"
    private const val ACTION_UNINSTALL_SHORTCUT = "com.android.launcher.action.UNINSTALL_SHORTCUT"
    private const val EXTRA_SHORTCUT_DUPLICATE = "duplicate"

    @Suppress("DEPRECATION")
    fun getLegacyShortcutPlacementIntent(context: Context, shortcut: ShortcutPlaceholder, install: Boolean): Intent {
        val shortcutIntent = ExecuteActivity.IntentBuilder(shortcut.id)
            .trigger(ShortcutTriggerType.LEGACY_SHORTCUT)
            .build(context)
        val addIntent = createIntent {
            putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
            putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcut.name)
            putExtra(EXTRA_SHORTCUT_DUPLICATE, true)
        }

        try {
            val iconUri = shortcut.icon.getIconURI(context, external = true)
            val scaledIcon = MediaStore.Images.Media.getBitmap(context.contentResolver, iconUri)
            val size = IconUtil.getIconSize(context, scaled = false)
            val unscaledIcon = Bitmap.createScaledBitmap(scaledIcon, size, size, false)
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, unscaledIcon)
        } catch (e: Exception) {
            addIntent.putExtra(
                Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(context.applicationContext, ShortcutIcon.NoIcon.iconResource),
            )
        }

        addIntent.action = if (install) ACTION_INSTALL_SHORTCUT else ACTION_UNINSTALL_SHORTCUT

        return addIntent
    }
}
