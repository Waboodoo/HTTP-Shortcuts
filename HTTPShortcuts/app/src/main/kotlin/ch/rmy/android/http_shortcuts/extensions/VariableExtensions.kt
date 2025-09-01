package ch.rmy.android.http_shortcuts.extensions

import ch.rmy.android.http_shortcuts.data.domains.variables.GlobalVariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKeyOrId
import ch.rmy.android.http_shortcuts.variables.VariableManager

fun Collection<VariableKeyOrId>.getGlobalVariables(
    variableManager: VariableManager,
): Set<GlobalVariableId> =
    mapNotNull { variableKeyOrId ->
        variableKeyOrId.globalVariableId
            ?: variableKeyOrId.variableKey?.let(variableManager::getGlobalVariableByKey)?.id
    }
        .toSet()
