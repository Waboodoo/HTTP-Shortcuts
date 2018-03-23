package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.realm.models.Shortcut

object IntentUtil {

    private const val ACTION_INSTALL_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT"
    private const val ACTION_UNINSTALL_SHORTCUT = "com.android.launcher.action.UNINSTALL_SHORTCUT"
    private const val EXTRA_SHORTCUT_DUPLICATE = "duplicate"

    fun getShortcutId(intent: Intent): Long {
        var shortcutId = -1L
        val uri = intent.data
        if (uri != null) {
            try {
                val id = uri.lastPathSegment
                shortcutId = java.lang.Long.parseLong(id)
            } catch (e: NumberFormatException) {
            }
        }
        if (shortcutId == -1L) {
            return intent.getLongExtra(ExecuteActivity.EXTRA_SHORTCUT_ID, -1L) // for backwards compatibility
        }
        return shortcutId
    }

    fun getVariableValues(intent: Intent): Map<String, String> {
        val serializable = intent.getSerializableExtra(ExecuteActivity.EXTRA_VARIABLE_VALUES)
        if (serializable is Map<*, *>) {
            @Suppress("UNCHECKED_CAST")
            return serializable as Map<String, String>
        }
        return emptyMap()
    }

    @Suppress("DEPRECATION")
    fun getShortcutPlacementIntent(context: Context, shortcut: Shortcut, install: Boolean): Intent {
        val shortcutIntent = ExecuteActivity.IntentBuilder(context, shortcut.id)
                .build()
        val addIntent = Intent()
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcut.name)
        addIntent.putExtra(EXTRA_SHORTCUT_DUPLICATE, true)
        if (shortcut.iconName != null) {
            val iconUri = shortcut.getIconURI(context)
            try {
                val icon = MediaStore.Images.Media.getBitmap(context.contentResolver, iconUri)
                addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, icon)
            } catch (e: Exception) {
                addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(context.applicationContext, ShortcutUIUtils.DEFAULT_ICON))
            }
        } else {
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(context.applicationContext, ShortcutUIUtils.DEFAULT_ICON))
        }

        addIntent.action = if (install) ACTION_INSTALL_SHORTCUT else ACTION_UNINSTALL_SHORTCUT

        return addIntent
    }

}
