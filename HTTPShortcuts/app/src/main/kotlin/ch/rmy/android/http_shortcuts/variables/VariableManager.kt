package ch.rmy.android.http_shortcuts.variables

import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.http_shortcuts.data.domains.variables.GlobalVariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKeyOrId
import ch.rmy.android.http_shortcuts.data.models.GlobalVariable
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import org.json.JSONObject

class VariableManager(
    globalVariables: List<GlobalVariable>,
    preResolvedValues: Map<VariableKey, String> = emptyMap(),
) {
    var globalVariables = globalVariables
        private set

    private val globalVariablesById: MutableMap<GlobalVariableId, GlobalVariable> = globalVariables.associateBy { it.id }.toMutableMap()
    private val globalVariablesByKey: MutableMap<VariableKey, GlobalVariable> = globalVariables.associateBy { it.key }.toMutableMap()
    private val variableValuesByKey = mutableMapOf<VariableKey, String>()

    init {
        preResolvedValues.forEach { (variableKeyOrId, value) ->
            val variable = getGlobalVariableByKeyOrId(variableKeyOrId) ?: return@forEach
            variableValuesByKey[variable.key] = encodeValue(variable, value)
        }
    }

    fun getGlobalVariableById(id: GlobalVariableId): GlobalVariable? =
        globalVariablesById[id]

    fun getGlobalVariableByKey(key: VariableKey): GlobalVariable? =
        globalVariablesByKey[key]

    fun getGlobalVariableByKeyOrId(keyOrId: VariableKeyOrId): GlobalVariable? =
        if (globalVariablesById.containsKey(keyOrId)) {
            getGlobalVariableById(keyOrId)
        } else {
            getGlobalVariableByKey(keyOrId)
        }

    fun getGlobalVariableValueById(globalVariableId: GlobalVariableId): String? =
        getGlobalVariableById(globalVariableId)?.key
            ?.let { variableKey ->
                getVariableValueByKey(variableKey)
            }

    fun getVariableValueByKey(variableKey: VariableKey): String? =
        variableValuesByKey[variableKey]

    fun getVariableValueByKeyOrId(variableKeyOrId: VariableKeyOrId): String? =
        getGlobalVariableByKeyOrId(variableKeyOrId)?.key
            ?.let { variableKey ->
                getVariableValueByKey(variableKey)
            }

    fun setGlobalVariableValue(variable: GlobalVariable, value: String, storeOnly: Boolean = false) {
        val newVariable = variable.copy(value = value)
        this@VariableManager.globalVariables = this@VariableManager.globalVariables.map { if (it.id == variable.id) newVariable else it }
        globalVariablesById[variable.id] = newVariable
        globalVariablesByKey[variable.key] = newVariable
        if (!storeOnly) {
            variableValuesByKey[variable.key] = encodeValue(variable, value)
        }
    }

    fun setVariableValueByKeyOrId(variableKeyOrId: VariableKeyOrId, value: String, storeOnly: Boolean = false) {
        getGlobalVariableByKeyOrId(variableKeyOrId)
            ?.let { variable ->
                setGlobalVariableValue(variable, value, storeOnly)
            }
    }

    fun getVariableValuesByIds(): Map<GlobalVariableId, String> =
        variableValuesByKey
            .mapKeys { entry ->
                getGlobalVariableByKey(entry.key)!!.id
            }

    fun getVariableValuesByKeys(): Map<VariableKey, String> =
        variableValuesByKey

    fun isResolved(variableKey: VariableKey): Boolean =
        variableValuesByKey.containsKey(variableKey)

    private fun encodeValue(variable: GlobalVariable, value: String) =
        value
            .runIf(variable.jsonEncode) {
                JSONObject.quote(this).drop(1).dropLast(1)
            }
            .runIf(variable.urlEncode) {
                try {
                    URLEncoder.encode(this, "utf-8")
                } catch (_: UnsupportedEncodingException) {
                    this
                }
            }
}
