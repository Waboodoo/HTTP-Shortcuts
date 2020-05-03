package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.os.Build
import androidx.annotation.RequiresApi
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.logException
import ch.rmy.android.http_shortcuts.extensions.mapIf

object LauncherShortcutManager {

    private const val ID_PREFIX = "shortcut_"

    fun supportsLauncherShortcuts() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1

    fun updateAppShortcuts(context: Context, categories: Collection<Category>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            update(context, categories)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun update(context: Context, categories: Collection<Category>) {
        try {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)!!
            val max = try {
                shortcutManager.maxShortcutCountPerActivity
            } catch (e: Exception) {
                logException(e)
                5
            }

            val launcherShortcuts = createLauncherShortcuts(context, categories, max)
            if (launcherShortcuts.isEmpty() && shortcutManager.dynamicShortcuts.isEmpty()) {
                return
            }
            shortcutManager.dynamicShortcuts = launcherShortcuts
        } catch (e: Exception) {
            logException(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun createLauncherShortcuts(context: Context, categories: Collection<Category>, max: Int): List<ShortcutInfo> {
        var count = 0
        val launcherShortcuts = mutableListOf<ShortcutInfo>()
        for (category in categories) {
            for (shortcut in category.shortcuts) {
                if (shortcut.launcherShortcut) {
                    val rank = max - count + 1
                    launcherShortcuts.add(createShortcutInfo(
                        context = context,
                        shortcutId = shortcut.id,
                        shortcutName = shortcut.name,
                        shortcutIcon = shortcut.iconName,
                        rank = rank
                    ))
                    if (++count >= max) {
                        return launcherShortcuts
                    }
                }
            }
        }
        return launcherShortcuts
    }

    private fun createShortcutInfo(context: Context, shortcut: Shortcut) = createShortcutInfo(context, shortcut.id, shortcut.name, shortcut.iconName)

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun createShortcutInfo(context: Context, shortcutId: String, shortcutName: String, shortcutIcon: String?, rank: Int = 0): ShortcutInfo {
        val icon = IconUtil.getIcon(context, shortcutIcon)
        return ShortcutInfo.Builder(context, ID_PREFIX + shortcutId)
            .setShortLabel(shortcutName)
            .setLongLabel(shortcutName)
            .setRank(rank)
            .setIntent(
                ExecuteActivity.IntentBuilder(context, shortcutId)
                    .build()
            )
            .mapIf(icon != null) {
                it.setIcon(icon)
            }
            .build()
    }

    fun supportsPinning(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java) !!
            if (shortcutManager.isRequestPinShortcutSupported) {
                return true
            }
        }
        return false
    }

    fun pinShortcut(context: Context, shortcut: Shortcut) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)!!
            val shortcutInfo = createShortcutInfo(context, shortcut)
            shortcutManager.requestPinShortcut(shortcutInfo, null)
        }
    }

    fun createShortcutPinIntent(context: Context, shortcut: Shortcut): Intent {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)!!
            val shortcutInfo = createShortcutInfo(context, shortcut)
            return shortcutManager.createShortcutResultIntent(shortcutInfo)
        }
        throw RuntimeException()
    }

    fun updatePinnedShortcut(context: Context, shortcutId: String, shortcutName: String, shortcutIcon: String?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)!!
            val shortcutInfo = createShortcutInfo(context, shortcutId, shortcutName, shortcutIcon)
            shortcutManager.updateShortcuts(listOf(shortcutInfo))
        }
    }

}
