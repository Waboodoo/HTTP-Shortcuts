package ch.rmy.android.http_shortcuts.variables

import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKeyOrId
import ch.rmy.android.http_shortcuts.data.models.VariableModel
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

class VariableManager(
    variables: List<VariableModel>,
    preResolvedValues: Map<VariableKey, String> = emptyMap(),
) : VariableLookup {

    private val variablesById = variables.associateBy { it.id }

    private val variablesByKey = variables.associateBy { it.key }

    private val variableValuesById = mutableMapOf<String, String>()

    init {
        preResolvedValues.forEach { (variableKeyOrId, value) ->
            val id = getVariableByKeyOrId(variableKeyOrId)?.id ?: return@forEach
            variableValuesById[id] = value
        }
    }

    override fun getVariableById(id: String): VariableModel? =
        variablesById[id]

    override fun getVariableByKey(key: String): VariableModel? =
        variablesByKey[key]

    fun getVariableByKeyOrId(keyOrId: VariableKeyOrId): VariableModel? =
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

    fun setVariableValue(variable: VariableModel, value: String, storeOnly: Boolean = false) {
        variable.value = value
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

    fun getVariableValues(): Map<VariableModel, String> =
        variableValuesById
            .mapKeys { entry ->
                getVariableById(entry.key)!!
            }

    fun isResolved(variableId: VariableId): Boolean =
        variableId in variableValuesById.keys

    companion object {
        internal fun encodeValue(variable: VariableModel, value: String) =
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
