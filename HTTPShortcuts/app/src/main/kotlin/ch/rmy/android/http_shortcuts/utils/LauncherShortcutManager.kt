package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.os.Build
import androidx.annotation.RequiresApi
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.mapIfNotNull
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.activities.main.MainActivity
import ch.rmy.android.http_shortcuts.data.dtos.LauncherShortcut
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

object LauncherShortcutManager {

    private const val ID_PREFIX_SHORTCUT = "shortcut_"
    private const val ID_PREFIX_CATEGORY = "category_"

    fun supportsLauncherShortcuts() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1

    fun updateAppShortcuts(context: Context, shortcuts: Collection<LauncherShortcut>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            update(context, shortcuts)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun update(context: Context, shortcuts: Collection<LauncherShortcut>) {
        try {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)!!
            val max = try {
                shortcutManager.maxShortcutCountPerActivity
            } catch (e: Exception) {
                logException(e)
                5
            }

            val launcherShortcuts = createLauncherShortcuts(context, shortcuts.take(max))
            if (launcherShortcuts.isEmpty() && shortcutManager.dynamicShortcuts.isEmpty()) {
                return
            }
            shortcutManager.dynamicShortcuts = launcherShortcuts
        } catch (e: Exception) {
            logException(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun createLauncherShortcuts(context: Context, shortcuts: Collection<LauncherShortcut>): List<ShortcutInfo> =
        shortcuts
            .mapIndexed { index, launcherShortcut ->
                createShortcutInfo(
                    context = context,
                    shortcutId = launcherShortcut.id,
                    shortcutName = launcherShortcut.name,
                    shortcutIcon = launcherShortcut.icon,
                    rank = index,
                )
            }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun createShortcutInfo(context: Context, shortcut: LauncherShortcut) =
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
                ExecuteActivity.IntentBuilder(shortcutId)
                    .build(context)
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

    fun pinShortcut(context: Context, shortcut: LauncherShortcut) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)!!
            val shortcutInfo = createShortcutInfo(context, shortcut)
            shortcutManager.requestPinShortcut(shortcutInfo, null)
        }
    }

    fun createShortcutPinIntent(context: Context, shortcut: LauncherShortcut): Intent {
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

    fun pinCategory(context: Context, categoryId: String, categoryName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)!!
            val shortcutInfo = createCategoryShortcutInfo(context, categoryId, categoryName)
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
                MainActivity.IntentBuilder()
                    .categoryId(categoryId)
                    .build(context)
            )
            .setIcon(IconUtil.getIcon(context, ShortcutIcon.BuiltInIcon("flat_grey_folder"))) // TODO
            .build()
}
