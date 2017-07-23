package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.os.Build
import android.support.annotation.RequiresApi
import ch.rmy.android.http_shortcuts.realm.models.Category
import ch.rmy.android.http_shortcuts.realm.models.Shortcut
import com.bugsnag.android.Bugsnag
import java.util.*

object LauncherShortcutManager {

    private const val ID_PREFIX = "shortcut_"

    fun supportsLauncherShortcuts() = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1

    fun updateAppShortcuts(context: Context, categories: Collection<Category>) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
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
                Bugsnag.notify(e)
                5
            }

            val launcherShortcuts = createLauncherShortcuts(context, categories, max)
            shortcutManager.dynamicShortcuts = launcherShortcuts
        } catch (e: Exception) {
            Bugsnag.notify(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun createLauncherShortcuts(context: Context, categories: Collection<Category>, max: Int): List<ShortcutInfo> {
        var count = 0
        val launcherShortcuts = ArrayList<ShortcutInfo>()
        for (category in categories) {
            for (shortcut in category.shortcuts) {
                if (shortcut.isLauncherShortcut) {
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
    private fun createShortcutInfo(context: Context, shortcut: Shortcut, rank: Int): ShortcutInfo {
        var builder: ShortcutInfo.Builder = ShortcutInfo.Builder(context, ID_PREFIX + shortcut.id)
                .setShortLabel(shortcut.name)
                .setLongLabel(shortcut.name)
                .setRank(rank)
                .setIntent(IntentUtil.createIntent(context, shortcut.id))
        val icon = shortcut.getIcon(context)
        if (icon != null) {
            builder = builder.setIcon(icon)
        }
        return builder.build()
    }

}
