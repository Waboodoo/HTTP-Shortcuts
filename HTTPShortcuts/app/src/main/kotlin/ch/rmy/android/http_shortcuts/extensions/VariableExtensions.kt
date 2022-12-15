package ch.rmy.android.http_shortcuts.extensions

import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.VariableResolver

suspend fun VariableResolver.resolve(
    variableManager: VariableManager,
    shortcut: ShortcutModel,
): VariableManager {
    val requiredVariableIds = VariableResolver.extractVariableIds(shortcut, variableManager, includeScripting = false)
        .toMutableSet()
    return resolve(variableManager, requiredVariableIds)
}
