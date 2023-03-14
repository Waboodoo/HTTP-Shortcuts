package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.activities.main.MainActivity
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.dtos.LauncherShortcut
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import javax.inject.Inject

class LauncherShortcutManager
@Inject
constructor(
    private val context: Context,
) {
    private val shortcutManager = context.getSystemService<ShortcutManager>()!!

    fun supportsLauncherShortcuts() =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1

    fun updateAppShortcuts(shortcuts: Collection<LauncherShortcut>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            update(shortcuts)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun update(shortcuts: Collection<LauncherShortcut>) {
        try {
            val max = try {
                shortcutManager.maxShortcutCountPerActivity
            } catch (e: Exception) {
                logException(e)
                5
            }

            val launcherShortcuts = createLauncherShortcuts(shortcuts.take(max))
            if (launcherShortcuts.isEmpty() && shortcutManager.dynamicShortcuts.isEmpty()) {
                return
            }
            shortcutManager.dynamicShortcuts = launcherShortcuts
        } catch (e: Exception) {
            logException(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun createLauncherShortcuts(shortcuts: Collection<LauncherShortcut>): List<ShortcutInfo> =
        shortcuts
            .mapIndexed { index, launcherShortcut ->
                createShortcutInfo(
                    shortcutId = launcherShortcut.id,
                    shortcutName = launcherShortcut.name,
                    shortcutIcon = launcherShortcut.icon,
                    rank = index,
                    trigger = ShortcutTriggerType.APP_SHORTCUT,
                )
            }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun createShortcutInfo(shortcut: LauncherShortcut, trigger: ShortcutTriggerType) =
        createShortcutInfo(shortcut.id, shortcut.name, shortcut.icon, trigger = trigger)

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun createShortcutInfo(
        shortcutId: ShortcutId,
        shortcutName: String,
        shortcutIcon: ShortcutIcon,
        rank: Int = 0,
        trigger: ShortcutTriggerType,
    ): ShortcutInfo {
        val icon = IconUtil.getIcon(context, shortcutIcon)
        val label = shortcutName.ifEmpty { "-" }
        return ShortcutInfo.Builder(context, ID_PREFIX_SHORTCUT + shortcutId)
            .setShortLabel(label)
            .setLongLabel(label)
            .setRank(rank)
            .setIntent(
                ExecuteActivity.IntentBuilder(shortcutId)
                    .trigger(trigger)
                    .build(context)
            )
            .runIfNotNull(icon) {
                setIcon(it)
            }
            .build()
    }

    fun supportsPinning(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (shortcutManager.isRequestPinShortcutSupported) {
                return true
            }
        }
        return false
    }

    fun pinShortcut(shortcut: LauncherShortcut) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutInfo = createShortcutInfo(shortcut, trigger = ShortcutTriggerType.HOME_SCREEN_SHORTCUT)
            shortcutManager.requestPinShortcut(shortcutInfo, null)
        }
    }

    fun createShortcutPinIntent(shortcut: LauncherShortcut): Intent {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutInfo = createShortcutInfo(shortcut, trigger = ShortcutTriggerType.HOME_SCREEN_SHORTCUT)
            return shortcutManager.createShortcutResultIntent(shortcutInfo)
        }
        throw RuntimeException()
    }

    fun updatePinnedShortcut(shortcutId: ShortcutId, shortcutName: String, shortcutIcon: ShortcutIcon) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutInfo = createShortcutInfo(shortcutId, shortcutName, shortcutIcon, trigger = ShortcutTriggerType.HOME_SCREEN_SHORTCUT)
            shortcutManager.updateShortcuts(listOf(shortcutInfo))
        }
    }

    fun pinCategory(categoryId: CategoryId, categoryName: String, shortcutIcon: ShortcutIcon) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutInfo = createCategoryShortcutInfo(categoryId, categoryName, shortcutIcon)
            shortcutManager.requestPinShortcut(shortcutInfo, null)
        }
    }

    fun updatePinnedCategoryShortcut(categoryId: CategoryId, categoryName: String, icon: ShortcutIcon) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutInfo = createCategoryShortcutInfo(categoryId, categoryName, icon)
            shortcutManager.updateShortcuts(listOf(shortcutInfo))
        }
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun createCategoryShortcutInfo(
        categoryId: CategoryId,
        categoryName: String,
        icon: ShortcutIcon,
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
            .setIcon(IconUtil.getIcon(context, icon))
            .build()

    companion object {
        private const val ID_PREFIX_SHORTCUT = "shortcut_"
        private const val ID_PREFIX_CATEGORY = "category_"
    }
}
