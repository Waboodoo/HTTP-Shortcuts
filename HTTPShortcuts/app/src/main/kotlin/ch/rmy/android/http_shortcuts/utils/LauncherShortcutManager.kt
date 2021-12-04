package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.os.Build
import androidx.annotation.RequiresApi
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.activities.main.MainActivity
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.logException
import ch.rmy.android.http_shortcuts.extensions.mapIfNotNull
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

object LauncherShortcutManager {

    private const val ID_PREFIX_SHORTCUT = "shortcut_"
    private const val ID_PREFIX_CATEGORY = "category_"

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
                    launcherShortcuts.add(
                        createShortcutInfo(
                            context = context,
                            shortcutId = shortcut.id,
                            shortcutName = shortcut.name,
                            shortcutIcon = shortcut.icon,
                            rank = rank,
                        )
                    )
                    if (++count >= max) {
                        return launcherShortcuts
                    }
                }
            }
        }
        return launcherShortcuts
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun createShortcutInfo(context: Context, shortcut: Shortcut) =
        createShortcutInfo(context, shortcut.id, shortcut.name, shortcut.icon)

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun createShortcutInfo(
        context: Context,
        shortcutId: String,
        shortcutName: String,
        shortcutIcon: ShortcutIcon,
        rank: Int = 0,
    ): ShortcutInfo {
        val icon = IconUtil.getIcon(context, shortcutIcon)
        val label = shortcutName.ifEmpty { "-" }
        return ShortcutInfo.Builder(context, ID_PREFIX_SHORTCUT + shortcutId)
            .setShortLabel(label)
            .setLongLabel(label)
            .setRank(rank)
            .setIntent(
                ExecuteActivity.IntentBuilder(context, shortcutId)
                    .build()
            )
            .mapIfNotNull(icon) {
                setIcon(it)
            }
            .build()
    }

    fun supportsPinning(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)!!
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

    fun updatePinnedShortcut(context: Context, shortcutId: String, shortcutName: String, shortcutIcon: ShortcutIcon) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)!!
            val shortcutInfo = createShortcutInfo(context, shortcutId, shortcutName, shortcutIcon)
            shortcutManager.updateShortcuts(listOf(shortcutInfo))
        }
    }

    fun pinCategory(context: Context, category: Category) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)!!
            val shortcutInfo = createCategoryShortcutInfo(context, category)
            shortcutManager.requestPinShortcut(shortcutInfo, null)
        }
    }

    fun updatePinnedCategoryShortcut(context: Context, categoryId: String, categoryName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)!!
            val shortcutInfo = createCategoryShortcutInfo(context, categoryId, categoryName)
            shortcutManager.updateShortcuts(listOf(shortcutInfo))
        }
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun createCategoryShortcutInfo(context: Context, category: Category) =
        createCategoryShortcutInfo(context, category.id, category.name)

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun createCategoryShortcutInfo(
        context: Context,
        categoryId: String,
        categoryName: String,
    ): ShortcutInfo =
        ShortcutInfo.Builder(context, ID_PREFIX_CATEGORY + categoryId)
            .setShortLabel(categoryName)
            .setLongLabel(categoryName)
            .setRank(0)
            .setIntent(
                MainActivity.IntentBuilder(context)
                    .categoryId(categoryId)
                    .build()
            )
            .setIcon(IconUtil.getIcon(context, ShortcutIcon.BuiltInIcon("flat_grey_folder"))) // TODO
            .build()
}
