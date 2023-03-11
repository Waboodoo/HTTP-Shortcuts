package ch.rmy.android.http_shortcuts.extensions

import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.VariableResolver

suspend fun VariableResolver.resolve(
    variableManager: VariableManager,
    shortcut: Shortcut,
): VariableManager {
    val requiredVariableIds = VariableResolver.extractVariableIds(shortcut, variableManager, includeScripting = false)
    return resolve(variableManager, requiredVariableIds)
}
