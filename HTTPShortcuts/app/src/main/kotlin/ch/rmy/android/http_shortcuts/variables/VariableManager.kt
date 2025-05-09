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

    private val variablesById: MutableMap<GlobalVariableId, GlobalVariable> = globalVariables.associateBy { it.id }.toMutableMap()
    private val variablesByKey: MutableMap<VariableKey, GlobalVariable> = globalVariables.associateBy { it.key }.toMutableMap()
    private val variableValuesById = mutableMapOf<String, String>()

    init {
        preResolvedValues.forEach { (variableKeyOrId, value) ->
            val variable = getGlobalVariableByKeyOrId(variableKeyOrId) ?: return@forEach
            variableValuesById[variable.id] = encodeValue(variable, value)
        }
    }

    fun getGlobalVariableById(id: GlobalVariableId): GlobalVariable? =
        variablesById[id]

    fun getGlobalVariableByKey(key: VariableKey): GlobalVariable? =
        variablesByKey[key]

    fun getGlobalVariableByKeyOrId(keyOrId: VariableKeyOrId): GlobalVariable? =
        if (variablesById.containsKey(keyOrId)) {
            getGlobalVariableById(keyOrId)
        } else {
            getGlobalVariableByKey(keyOrId)
        }

    fun getGlobalVariableValueById(globalVariableId: GlobalVariableId): String? =
        variableValuesById[globalVariableId]

    fun getVariableValueByKey(variableKey: VariableKey): String? =
        getGlobalVariableByKey(variableKey)?.id
            ?.let { variableId ->
                getGlobalVariableValueById(variableId)
            }

    fun getVariableValueByKeyOrId(variableKeyOrId: VariableKeyOrId): String? =
        getGlobalVariableByKeyOrId(variableKeyOrId)?.id
            ?.let { variableId ->
                getGlobalVariableValueById(variableId)
            }

    fun setGlobalVariableValue(variable: GlobalVariable, value: String, storeOnly: Boolean = false) {
        val newVariable = variable.copy(value = value)
        this@VariableManager.globalVariables = this@VariableManager.globalVariables.map { if (it.id == variable.id) newVariable else it }
        variablesById[variable.id] = newVariable
        variablesByKey[variable.key] = newVariable
        if (!storeOnly) {
            variableValuesById[variable.id] = encodeValue(variable, value)
        }
    }

    fun setVariableValueByKeyOrId(variableKeyOrId: VariableKeyOrId, value: String, storeOnly: Boolean = false) {
        getGlobalVariableByKeyOrId(variableKeyOrId)
            ?.let { variable ->
                setGlobalVariableValue(variable, value, storeOnly)
            }
    }

    fun getVariableValuesByIds(): Map<GlobalVariableId, String> =
        variableValuesById

    fun getVariableValuesByKeys(): Map<VariableKey, String> =
        variableValuesById
            .mapKeys { entry ->
                getGlobalVariableById(entry.key)!!.key
            }

    fun isResolved(globalVariableId: GlobalVariableId): Boolean =
        variableValuesById.containsKey(globalVariableId)

    companion object {
        internal fun encodeValue(variable: GlobalVariable, value: String) =
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
}
