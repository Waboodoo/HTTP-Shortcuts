package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.content.Intent
import android.content.pm.Capability
import android.content.pm.CapabilityParams
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.os.Build
import androidx.annotation.WorkerThread
import androidx.core.content.getSystemService
import androidx.core.content.pm.ShortcutManagerCompat.EXTRA_SHORTCUT_ID
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.http_shortcuts.R
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
    private val shortcutManager
        get() = context.getSystemService<ShortcutManager>()!!

    fun supportsDirectShare() =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

    fun reportUse(shortcutId: ShortcutId) {
        shortcutManager.reportShortcutUsed(createShortcutInfoId(shortcutId))
    }

    @WorkerThread
    fun updateAppShortcuts(shortcuts: Collection<LauncherShortcut>) {
        update(shortcuts)
    }

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

    private fun createLauncherShortcuts(shortcuts: Collection<LauncherShortcut>): List<ShortcutInfo> =
        shortcuts
            .mapIndexed { index, launcherShortcut ->
                createShortcutInfo(
                    launcherShortcut = launcherShortcut,
                    rank = index,
                    trigger = ShortcutTriggerType.APP_SHORTCUT,
                )
            }

    private fun createShortcutInfo(
        launcherShortcut: LauncherShortcut,
        rank: Int = 0,
        trigger: ShortcutTriggerType,
    ): ShortcutInfo {
        val icon = IconUtil.getIcon(context, launcherShortcut.icon, adaptive = true)
        val label = launcherShortcut.name.ifEmpty { "-" }
        return ShortcutInfo.Builder(context, createShortcutInfoId(launcherShortcut.id))
            .setShortLabel(label)
            .setLongLabel(label)
            .setRank(rank)
            .setIntent(
                ExecuteActivity.IntentBuilder(launcherShortcut.id)
                    .trigger(trigger)
                    .build(context),
            )
            .runIfNotNull(icon) {
                setIcon(it)
            }
            .setCategories(
                buildSet {
                    if (launcherShortcut.isTextShareTarget) {
                        add("ch.rmy.android.http_shortcuts.directshare.category.TEXT_SHARE_TARGET")
                    }
                    if (launcherShortcut.isFileShareTarget) {
                        add("ch.rmy.android.http_shortcuts.directshare.category.FILE_SHARE_TARGET")
                    }
                },
            )
            .run {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    addCapabilityBinding(
                        Capability.Builder("custom.actions.intent.RUN_SHORTCUT").build(),
                        CapabilityParams.Builder("shortcutName", label)
                            .build(),
                    )
                } else {
                    this
                }
            }
            .run {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    setLongLived(true)
                } else {
                    this
                }
            }
            .build()
    }

    fun supportsPinning(): Boolean =
        shortcutManager.isRequestPinShortcutSupported

    fun pinShortcut(shortcut: LauncherShortcut) {
        logInfo("Pinning shortcut")
        val shortcutInfo = createShortcutInfo(shortcut, trigger = ShortcutTriggerType.HOME_SCREEN_SHORTCUT)
        shortcutManager.requestPinShortcut(shortcutInfo, null)
    }

    fun createShortcutPinIntent(shortcut: LauncherShortcut): Intent {
        val shortcutInfo = createShortcutInfo(shortcut, trigger = ShortcutTriggerType.HOME_SCREEN_SHORTCUT)
        return shortcutManager.createShortcutResultIntent(shortcutInfo)
    }

    fun updatePinnedShortcut(shortcut: LauncherShortcut) {
        logInfo("Updating pinned shortcut")
        val shortcutInfo = createShortcutInfo(shortcut, trigger = ShortcutTriggerType.HOME_SCREEN_SHORTCUT)
        shortcutManager.updateShortcuts(listOf(shortcutInfo))
    }

    fun pinCategory(categoryId: CategoryId, categoryName: String, shortcutIcon: ShortcutIcon) {
        val shortcutInfo = createCategoryShortcutInfo(categoryId, categoryName, shortcutIcon)
        shortcutManager.requestPinShortcut(shortcutInfo, null)
    }

    fun updatePinnedCategoryShortcut(categoryId: CategoryId, categoryName: String, icon: ShortcutIcon) {
        val shortcutInfo = createCategoryShortcutInfo(categoryId, categoryName, icon)
        shortcutManager.updateShortcuts(listOf(shortcutInfo))
    }

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
                    .build(context),
            )
            .runIfNotNull(IconUtil.getIcon(context, icon, adaptive = true)) {
                setIcon(it)
            }
            .build()

    fun removeShortcut(shortcutId: ShortcutId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val ids = listOf(createShortcutInfoId(shortcutId))
            shortcutManager.removeLongLivedShortcuts(ids)
            shortcutManager.removeDynamicShortcuts(ids)
            shortcutManager.disableShortcuts(ids, context.getString(R.string.error_shortcut_deleted))
        }
    }

    private fun createShortcutInfoId(shortcutId: ShortcutId): String =
        ID_PREFIX_SHORTCUT + shortcutId

    companion object {
        private const val ID_PREFIX_SHORTCUT = "shortcut_"
        private const val ID_PREFIX_CATEGORY = "category_"

        fun Intent.extractShortcutId(): ShortcutId? =
            getStringExtra(EXTRA_SHORTCUT_ID)?.removePrefix(ID_PREFIX_SHORTCUT)
    }
}
