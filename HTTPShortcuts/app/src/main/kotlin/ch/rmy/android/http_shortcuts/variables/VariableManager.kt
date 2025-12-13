package ch.rmy.android.http_shortcuts.variables

import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.http_shortcuts.data.domains.variables.GlobalVariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKeyOrId
import ch.rmy.android.http_shortcuts.data.models.GlobalVariable
import ch.rmy.android.http_shortcuts.http.RequestUtil
import org.json.JSONObject

class VariableManager(
    globalVariables: List<GlobalVariable>,
    preResolvedValues: Map<VariableKeyOrId, String> = emptyMap(),
) {
    var globalVariables = globalVariables
        private set

    private val globalVariablesById: MutableMap<GlobalVariableId, GlobalVariable> = globalVariables.associateBy { it.id }.toMutableMap()
    private val globalVariablesByKey: MutableMap<VariableKey, GlobalVariable> = globalVariables.associateBy { it.key }.toMutableMap()

    private val globalVariableValues = mutableMapOf<GlobalVariableId, String>()
    private val localVariablesValues = mutableMapOf<VariableKey, String>()

    init {
        preResolvedValues.forEach { (variableKeyOrId, value) ->
            val variable = getGlobalVariableByKeyOrId(variableKeyOrId)
            if (variable != null) {
                globalVariableValues[variable.id] = encodeValue(variable, value)
            } else {
                variableKeyOrId.variableKey?.let { variableKey ->
                    localVariablesValues[variableKey] = value
                }
            }
        }
    }

    fun getGlobalVariableById(id: GlobalVariableId): GlobalVariable? =
        globalVariablesById[id]

    fun getGlobalVariableByKey(key: VariableKey): GlobalVariable? =
        globalVariablesByKey[key]

    fun getGlobalVariableByKeyOrId(keyOrId: VariableKeyOrId): GlobalVariable? =
        keyOrId.globalVariableId?.let(::getGlobalVariableById)
            ?: keyOrId.variableKey?.let(::getGlobalVariableByKey)

    fun setGlobalVariableValue(variable: GlobalVariable, value: String, storeOnly: Boolean = false) {
        val newVariable = variable.copy(value = value)
        this@VariableManager.globalVariables = this@VariableManager.globalVariables.map { if (it.id == variable.id) newVariable else it }
        globalVariablesById[variable.id] = newVariable
        globalVariablesByKey[variable.key] = newVariable
        if (!storeOnly) {
            globalVariableValues[variable.id] = encodeValue(variable, value)
        }
    }

    fun setVariableValueByKeyOrId(variableKeyOrId: VariableKeyOrId, value: String, storeOnly: Boolean = false) {
        val variable = getGlobalVariableByKeyOrId(variableKeyOrId)
        if (variable != null) {
            setGlobalVariableValue(variable, value, storeOnly)
        } else if (!storeOnly) {
            variableKeyOrId.variableKey?.let { variableKey ->
                localVariablesValues[variableKey] = value
            }
        }
    }

    fun getVariableValues(): ResolvedVariableValues =
        ResolvedVariableValues(
            globalVariableValues = globalVariableValues.toMap(),
            localVariablesValues = localVariablesValues.toMap(),
            globalVariableKeysToIds = globalVariables.associate { it.key to it.id },
        )

    fun isResolved(variable: GlobalVariable): Boolean =
        globalVariableValues.containsKey(variable.id)

    private fun encodeValue(variable: GlobalVariable, value: String) =
        value
            .runIf(variable.jsonEncode) {
                JSONObject.quote(this).drop(1).dropLast(1)
            }
            .runIf(variable.urlEncode) {
                RequestUtil.encode(this)
            }
}
