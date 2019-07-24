package ch.rmy.android.http_shortcuts.variables

import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.extensions.mapIf
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

class VariableManager(variables: List<Variable>) : VariableLookup {

    private val variablesById = variables.associateBy { it.id }

    private val variablesByKey = variables.associateBy { it.key }

    private val variableValuesById = mutableMapOf<String, String>()

    override fun getVariableById(id: String): Variable? =
        variablesById[id]

    override fun getVariableByKey(key: String): Variable? =
        variablesByKey[key]

    fun getVariableByKeyOrId(keyOrId: String): Variable? =
        if (variablesById.containsKey(keyOrId)) {
            getVariableById(keyOrId)
        } else {
            getVariableByKey(keyOrId)
        }

    fun getVariableValueById(variableId: String): String? =
        variableValuesById[variableId]

    fun getVariableValueByKey(variableKey: String): String? =
        getVariableByKey(variableKey)?.id
            ?.let { variableId ->
                getVariableValueById(variableId)
            }

    fun getVariableValueByKeyOrId(variableKeyOrId: String): String? =
        getVariableByKeyOrId(variableKeyOrId)?.id
            ?.let { variableId ->
                getVariableValueById(variableId)
            }

    fun setVariableValueByKey(variableKey: String, value: String) {
        getVariableByKey(variableKey)
            ?.let { variable ->
                setVariableValue(variable, value)
            }
    }

    fun setVariableValueById(variableId: String, value: String) {
        getVariableById(variableId)
            ?.let { variable ->
                setVariableValue(variable, value)
            }
    }

    fun setVariableValue(variable: Variable, value: String) {
        variableValuesById[variable.id] = encodeValue(variable, value)
    }

    fun setVariableValueByKeyOrId(variableKeyOrId: String, value: String) {
        getVariableByKeyOrId(variableKeyOrId)
            ?.let { variable ->
                setVariableValue(variable, value)
            }
    }

    fun getVariableValuesByIds(): Map<String, String> =
        variableValuesById

    fun getVariableValuesByKeys(): Map<String, String> =
        variableValuesById
            .mapKeys { entry ->
                getVariableById(entry.key)!!.key
            }

    companion object {
        private fun encodeValue(variable: Variable, value: String) =
            value
                .mapIf(variable.jsonEncode) {
                    JSONObject.quote(it).drop(1).dropLast(1)
                }
                .mapIf(variable.urlEncode) {
                    try {
                        URLEncoder.encode(it, "utf-8")
                    } catch (e: UnsupportedEncodingException) {
                        it
                    }
                }
    }

}