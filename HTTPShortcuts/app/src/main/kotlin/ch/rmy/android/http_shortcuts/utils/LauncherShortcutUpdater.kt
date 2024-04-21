package ch.rmy.android.http_shortcuts.utils

import android.content.Intent
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.dtos.LauncherShortcut
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.variables.VariableLookup
import ch.rmy.android.http_shortcuts.variables.VariableManager
import javax.inject.Inject

class LauncherShortcutUpdater
@Inject
constructor(
    private val categoryRepository: CategoryRepository,
    private val variableRepository: VariableRepository,
    private val shortcutRepository: ShortcutRepository,
    private val launcherShortcutManager: LauncherShortcutManager,
    private val shareUtil: ShareUtil,
) {
    suspend fun updateAppShortcuts(updatedShortcutId: ShortcutId? = null) {
        val categories = categoryRepository.getCategories()
        val variables = variableRepository.getVariables()
        val variableIds = shareUtil.getTextShareVariables(variables).map { it.id }.toSet()
        val launcherShortcuts = categories.flatMap(Category::shortcuts)
            .filter(Shortcut::launcherShortcut)
            .map { shortcut ->
                getLauncherShortcut(shortcut, variableIds, VariableManager(variables))
            }
        launcherShortcutManager.updateAppShortcuts(launcherShortcuts, updatedShortcutId)
    }

    suspend fun pinShortcut(shortcutId: ShortcutId) {
        if (!launcherShortcutManager.supportsPinning()) {
            return
        }
        launcherShortcutManager.pinShortcut(
            getLauncherShortcut(shortcutId),
        )
    }

    suspend fun updatePinnedShortcut(shortcutId: ShortcutId) {
        if (!launcherShortcutManager.supportsPinning()) {
            return
        }
        launcherShortcutManager.updatePinnedShortcut(
            getLauncherShortcut(shortcutId),
        )
    }

    private suspend fun getLauncherShortcut(shortcutId: ShortcutId): LauncherShortcut {
        val shortcut = shortcutRepository.getShortcutById(shortcutId)
        val variables = variableRepository.getVariables()
        val variableIds = shareUtil.getTextShareVariables(variables).map { it.id }.toSet()
        return getLauncherShortcut(shortcut, variableIds, VariableManager(variables))
    }

    private fun getLauncherShortcut(shortcut: Shortcut, variableIds: Set<VariableId>, variableLookup: VariableLookup): LauncherShortcut =
        LauncherShortcut(
            id = shortcut.id,
            name = shortcut.name,
            icon = shortcut.icon,
            isTextShareTarget = shareUtil.isTextShareTarget(shortcut, variableIds, variableLookup),
            isFileShareTarget = shareUtil.isFileShareTarget(shortcut),
        )

    suspend fun createShortcutPinIntent(shortcutId: ShortcutId): Intent =
        launcherShortcutManager.createShortcutPinIntent(getLauncherShortcut(shortcutId))
}
