package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.os.Build
import android.support.annotation.RequiresApi
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.realm.models.Category
import ch.rmy.android.http_shortcuts.realm.models.Shortcut
import java.util.*

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
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)
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
                    launcherShortcuts.add(createShortcutInfo(context, shortcut, rank))
                    if (++count >= max) {
                        return launcherShortcuts
                    }
                }
            }
        }
        return launcherShortcuts
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun createShortcutInfo(context: Context, shortcut: Shortcut, rank: Int = 0): ShortcutInfo {
        val icon = shortcut.getIcon(context)
        return ShortcutInfo.Builder(context, ID_PREFIX + shortcut.id)
                .setShortLabel(shortcut.name)
                .setLongLabel(shortcut.name)
                .setRank(rank)
                .setIntent(
                        ExecuteActivity.IntentBuilder(context, shortcut.id)
                                .build()
                )
                .mapIf(icon != null) {
                    it.setIcon(icon)
                }
                .build()
    }

    fun supportsPinning(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)
            if (shortcutManager.isRequestPinShortcutSupported) {
                return true
            }
        }
        return false
    }

    fun pinShortcut(context: Context, shortcut: Shortcut) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)
            val shortcutInfo = createShortcutInfo(context, shortcut)
            shortcutManager.requestPinShortcut(shortcutInfo, null)
        }
    }

    fun createShortcutPinIntent(context: Context, shortcut: Shortcut): Intent {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)
            val shortcutInfo = createShortcutInfo(context, shortcut)
            return shortcutManager.createShortcutResultIntent(shortcutInfo)
        }
        throw RuntimeException()
    }

    fun updatePinnedShortcut(context: Context, shortcut: Shortcut) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)
            val shortcutInfo = createShortcutInfo(context, shortcut)
            val list = Collections.singletonList(shortcutInfo)
            shortcutManager.updateShortcuts(list)
        }
    }

}
