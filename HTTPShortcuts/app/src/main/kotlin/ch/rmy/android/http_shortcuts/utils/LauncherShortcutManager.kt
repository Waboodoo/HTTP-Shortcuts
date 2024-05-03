package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.WorkerThread
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.content.pm.ShortcutManagerCompat.EXTRA_SHORTCUT_ID
import androidx.core.graphics.drawable.IconCompat
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.logInfo
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
    fun supportsLauncherShortcuts() =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1

    fun supportsDirectShare() =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

    @WorkerThread
    fun updateAppShortcuts(shortcuts: Collection<LauncherShortcut>, updatedShortcutId: ShortcutId? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            update(shortcuts, updatedShortcutId)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun update(shortcuts: Collection<LauncherShortcut>, updatedShortcutId: ShortcutId?) {
        try {
            val max = try {
                ShortcutManagerCompat.getMaxShortcutCountPerActivity(context)
            } catch (e: Exception) {
                logException(e)
                5
            }

            val shortcutInfos = createShortcutsInfos(shortcuts)
            if (shortcutInfos.isNotEmpty() || ShortcutManagerCompat.getDynamicShortcuts(context).isNotEmpty()) {
                ShortcutManagerCompat.setDynamicShortcuts(context, shortcutInfos.take(max))
            }

            updatedShortcutId
                ?.let(::createShortcutInfoId)
                ?.let { shortcutInfoId ->
                    shortcutInfos.find { it.id == shortcutInfoId }
                }
                ?.let { shortcutInfo ->
                    ShortcutManagerCompat.pushDynamicShortcut(context, shortcutInfo)
                }
        } catch (e: Exception) {
            logException(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun createShortcutsInfos(shortcuts: Collection<LauncherShortcut>): List<ShortcutInfoCompat> =
        shortcuts
            .mapIndexed { index, launcherShortcut ->
                createShortcutInfo(
                    launcherShortcut = launcherShortcut,
                    rank = index,
                    trigger = ShortcutTriggerType.APP_SHORTCUT,
                )
            }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun createShortcutInfo(
        launcherShortcut: LauncherShortcut,
        rank: Int = 0,
        trigger: ShortcutTriggerType,
    ): ShortcutInfoCompat {
        val icon = IconUtil.getIcon(context, launcherShortcut.icon, adaptive = true)
        val label = launcherShortcut.name.ifEmpty { "-" }
        return ShortcutInfoCompat.Builder(context, createShortcutInfoId(launcherShortcut.id))
            .setShortLabel(label)
            .setLongLabel(label)
            .setRank(rank)
            .setIntent(
                ExecuteActivity.IntentBuilder(launcherShortcut.id)
                    .trigger(trigger)
                    .build(context)
            )
            .runIfNotNull(icon) {
                val iconCompat = IconCompat.createFromIcon(context, it)
                    ?.takeIf { iconCompat ->
                        when (iconCompat.type) {
                            IconCompat.TYPE_BITMAP,
                            IconCompat.TYPE_ADAPTIVE_BITMAP,
                            IconCompat.TYPE_RESOURCE,
                            -> true
                            else -> {
                                logInfo("Unsupported icon: ${launcherShortcut.icon}")
                                logException(IllegalArgumentException("Unsupported icon type ${iconCompat.type}"))
                                false
                            }
                        }
                    }
                setIcon(iconCompat ?: IconCompat.createWithResource(context, ShortcutIcon.NoIcon.ICON_RESOURCE))
            }
            .setCategories(
                buildSet {
                    if (launcherShortcut.isTextShareTarget) {
                        add("ch.rmy.android.http_shortcuts.directshare.category.TEXT_SHARE_TARGET")
                    }
                    if (launcherShortcut.isFileShareTarget) {
                        add("ch.rmy.android.http_shortcuts.directshare.category.FILE_SHARE_TARGET")
                    }
                }
            )
            .run {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    addCapabilityBinding(
                        "custom.actions.intent.RUN_SHORTCUT",
                        "shortcutName",
                        listOf(label),
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

    fun supportsPinning(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
                return true
            }
        }
        return false
    }

    fun pinShortcut(shortcut: LauncherShortcut) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutInfo = createShortcutInfo(shortcut, trigger = ShortcutTriggerType.HOME_SCREEN_SHORTCUT)
            ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)
        }
    }

    fun createShortcutPinIntent(shortcut: LauncherShortcut): Intent {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutInfo = createShortcutInfo(shortcut, trigger = ShortcutTriggerType.HOME_SCREEN_SHORTCUT)
            return ShortcutManagerCompat.createShortcutResultIntent(context, shortcutInfo)
        }
        throw RuntimeException()
    }

    fun updatePinnedShortcut(shortcut: LauncherShortcut) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutInfo = createShortcutInfo(shortcut, trigger = ShortcutTriggerType.HOME_SCREEN_SHORTCUT)
            ShortcutManagerCompat.updateShortcuts(context, listOf(shortcutInfo))
        }
    }

    fun pinCategory(categoryId: CategoryId, categoryName: String, shortcutIcon: ShortcutIcon) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutInfo = createCategoryShortcutInfo(categoryId, categoryName, shortcutIcon)
            ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)
        }
    }

    fun updatePinnedCategoryShortcut(categoryId: CategoryId, categoryName: String, icon: ShortcutIcon) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutInfo = createCategoryShortcutInfo(categoryId, categoryName, icon)
            ShortcutManagerCompat.updateShortcuts(context, listOf(shortcutInfo))
        }
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun createCategoryShortcutInfo(
        categoryId: CategoryId,
        categoryName: String,
        icon: ShortcutIcon,
    ): ShortcutInfoCompat =
        ShortcutInfoCompat.Builder(context, ID_PREFIX_CATEGORY + categoryId)
            .setShortLabel(categoryName)
            .setLongLabel(categoryName)
            .setRank(0)
            .setIntent(
                MainActivity.IntentBuilder()
                    .categoryId(categoryId)
                    .build(context)
            )
            .runIfNotNull(IconUtil.getIcon(context, icon, adaptive = true)) {
                setIcon(IconCompat.createFromIcon(context, it))
            }
            .build()

    fun removeShortcut(shortcutId: ShortcutId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val ids = listOf(createShortcutInfoId(shortcutId))
            ShortcutManagerCompat.removeLongLivedShortcuts(context, ids)
            ShortcutManagerCompat.removeDynamicShortcuts(context, ids)
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
