package ch.rmy.android.http_shortcuts.utils

import android.content.Intent
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryRepository
import ch.rmy.android.http_shortcuts.data.domains.request_headers.RequestHeaderRepository
import ch.rmy.android.http_shortcuts.data.domains.request_parameters.RequestParameterRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.dtos.LauncherShortcut
import ch.rmy.android.http_shortcuts.data.models.RequestHeader
import ch.rmy.android.http_shortcuts.data.models.RequestParameter
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.getRequestParametersForShortcut
import ch.rmy.android.http_shortcuts.extensions.getRequestParametersForShortcuts
import ch.rmy.android.http_shortcuts.extensions.ids
import ch.rmy.android.http_shortcuts.variables.VariableLookup
import ch.rmy.android.http_shortcuts.variables.VariableManager
import javax.inject.Inject

class LauncherShortcutUpdater
@Inject
constructor(
    private val categoryRepository: CategoryRepository,
    private val variableRepository: VariableRepository,
    private val shortcutRepository: ShortcutRepository,
    private val requestHeaderRepository: RequestHeaderRepository,
    private val requestParameterRepository: RequestParameterRepository,
    private val launcherShortcutManager: LauncherShortcutManager,
    private val shareUtil: ShareUtil,
) {
    suspend fun updateAppShortcuts() {
        val categoryIds = categoryRepository.getCategoryIds()
        val shortcuts = shortcutRepository.getShortcuts()
        val shortcutIds = shortcuts.ids()
        val shortcutsByCategoryId = shortcuts.groupBy { it.categoryId }
        val variables = variableRepository.getVariables()
        val variableIds = shareUtil.getTextShareVariables(variables).ids()
        val headersByShortcutId = requestHeaderRepository.getRequestHeadersByShortcutIds(shortcutIds)
        val parametersById = requestParameterRepository.getRequestParametersForShortcuts(shortcuts)
        val launcherShortcuts = categoryIds
            .flatMap { categoryId ->
                shortcutsByCategoryId[categoryId] ?: emptyList()
            }
            .filter(Shortcut::launcherShortcut)
            .map { shortcut ->
                getLauncherShortcut(
                    shortcut = shortcut,
                    headers = headersByShortcutId[shortcut.id] ?: emptyList(),
                    parameters = parametersById[shortcut.id] ?: emptyList(),
                    variableIds = variableIds,
                    variableLookup = VariableManager(variables),
                )
            }
        launcherShortcutManager.updateAppShortcuts(launcherShortcuts)
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
        return getLauncherShortcut(
            shortcut = shortcut,
            headers = requestHeaderRepository.getRequestHeadersByShortcutId(shortcutId),
            parameters = requestParameterRepository.getRequestParametersForShortcut(shortcut),
            variableIds = shareUtil.getTextShareVariables(variables).ids(),
            variableLookup = VariableManager(variables),
        )
    }

    private suspend fun getLauncherShortcut(
        shortcut: Shortcut,
        headers: List<RequestHeader>,
        parameters: List<RequestParameter>,
        variableIds: Set<VariableId>,
        variableLookup: VariableLookup,
    ): LauncherShortcut =
        LauncherShortcut(
            id = shortcut.id,
            name = shortcut.name,
            icon = shortcut.icon,
            isTextShareTarget = shareUtil.isTextShareTarget(shortcut, headers, parameters, variableIds, variableLookup),
            isFileShareTarget = shareUtil.isFileShareTarget(shortcut, parameters),
        )

    suspend fun createShortcutPinIntent(shortcutId: ShortcutId): Intent =
        launcherShortcutManager.createShortcutPinIntent(getLauncherShortcut(shortcutId))
}
