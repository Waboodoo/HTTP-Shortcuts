package ch.rmy.android.http_shortcuts.variables

import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKeyOrId
import ch.rmy.android.http_shortcuts.data.models.Variable
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import org.json.JSONObject

class VariableManager(
    val variables: List<Variable>,
    preResolvedValues: Map<VariableKey, String> = emptyMap(),
) : VariableLookup {
    private val variablesById: Map<VariableId, Variable> = variables.associateBy { it.id }

    private val variablesByKey: Map<VariableKey, Variable> = variables.associateBy { it.key }

    private val variableValuesById = mutableMapOf<String, String>()

    init {
        preResolvedValues.forEach { (variableKeyOrId, value) ->
            val variable = getVariableByKeyOrId(variableKeyOrId) ?: return@forEach
            variableValuesById[variable.id] = encodeValue(variable, value)
        }
    }

    override fun getVariableById(id: String): Variable? =
        variablesById[id]

    override fun getVariableByKey(key: String): Variable? =
        variablesByKey[key]

    fun getVariableByKeyOrId(keyOrId: VariableKeyOrId): Variable? =
        if (variablesById.containsKey(keyOrId)) {
            getVariableById(keyOrId)
        } else {
            getVariableByKey(keyOrId)
        }

    fun getVariableValueById(variableId: VariableId): String? =
        variableValuesById[variableId]

    fun getVariableValueByKey(variableKey: String): String? =
        getVariableByKey(variableKey)?.id
            ?.let { variableId ->
                getVariableValueById(variableId)
            }

    fun getVariableValueByKeyOrId(variableKeyOrId: VariableKeyOrId): String? =
        getVariableByKeyOrId(variableKeyOrId)?.id
            ?.let { variableId ->
                getVariableValueById(variableId)
            }

    fun setVariableValue(variable: Variable, value: String, storeOnly: Boolean = false) {
        variable.valueOverride = value
        if (!storeOnly) {
            variableValuesById[variable.id] = encodeValue(variable, value)
        }
    }

    fun setVariableValueByKeyOrId(variableKeyOrId: VariableKeyOrId, value: String, storeOnly: Boolean = false) {
        getVariableByKeyOrId(variableKeyOrId)
            ?.let { variable ->
                setVariableValue(variable, value, storeOnly)
            }
    }

    fun getVariableValuesByIds(): Map<VariableId, String> =
        variableValuesById

    fun getVariableValuesByKeys(): Map<VariableKey, String> =
        variableValuesById
            .mapKeys { entry ->
                getVariableById(entry.key)!!.key
            }

    fun getVariableValues(): Map<Variable, String> =
        variableValuesById
            .mapKeys { entry ->
                getVariableById(entry.key)!!
            }

    fun isResolved(variableId: VariableId): Boolean =
        variableId in variableValuesById.keys

    companion object {
        internal fun encodeValue(variable: Variable, value: String) =
            value
                .runIf(variable.jsonEncode) {
                    JSONObject.quote(this).drop(1).dropLast(1)
                }
                .runIf(variable.urlEncode) {
                    try {
                        URLEncoder.encode(this, "utf-8")
                    } catch (e: UnsupportedEncodingException) {
                        this
                    }
                }
    }
}
