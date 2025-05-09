package ch.rmy.android.http_shortcuts.variables

import ch.rmy.android.http_shortcuts.data.domains.variables.GlobalVariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKeyOrId

data class ResolvedVariableValues(
    val globalVariableValues: Map<GlobalVariableId, String>,
    val localVariablesValues: Map<VariableKey, String>,
    val globalVariableKeysToIds: Map<VariableKey, GlobalVariableId>,
) {
    operator fun get(variableKeyOrId: VariableKeyOrId): String? =
        variableKeyOrId.globalVariableId
            ?.let { globalVariableValues[it] }
            ?: variableKeyOrId.variableKey
                ?.let { variableKey ->
                    globalVariableKeysToIds[variableKey]
                        ?.let { globalVariableId ->
                            globalVariableValues[globalVariableId]
                        }
                        ?: localVariablesValues[variableKey]
                }

    fun getAll(): Map<VariableKeyOrId, String> =
        buildMap {
            globalVariableValues.forEach { (globalVariableId, value) ->
                put(VariableKeyOrId(globalVariableId), value)
            }
            localVariablesValues.forEach { (localVariableKey, value) ->
                put(VariableKeyOrId(localVariableKey), value)
            }
        }

    companion object {
        val empty = ResolvedVariableValues(
            globalVariableValues = emptyMap(),
            localVariablesValues = emptyMap(),
            globalVariableKeysToIds = emptyMap(),
        )
    }
}
