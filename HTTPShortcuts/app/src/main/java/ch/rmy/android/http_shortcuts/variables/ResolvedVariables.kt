package ch.rmy.android.http_shortcuts.variables

import ch.rmy.android.http_shortcuts.realm.models.ResolvedVariable
import ch.rmy.android.http_shortcuts.realm.models.Variable
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.*

class ResolvedVariables {

    private val variableValues = HashMap<String, String>()

    fun hasValue(variableName: String): Boolean {
        return variableValues.containsKey(variableName)
    }

    fun getValue(variableName: String): String? {
        return variableValues[variableName]
    }

    fun toList(): List<ResolvedVariable> {
        val resolvedVariables = ArrayList<ResolvedVariable>()
        for ((key, value) in variableValues) {
            resolvedVariables.add(ResolvedVariable.createNew(key, value))
        }
        return resolvedVariables
    }

    class Builder {

        private val resolvedVariables: ResolvedVariables = ResolvedVariables()

        fun add(variable: Variable, value: String): Builder {
            var encodedValue = value
            if (variable.jsonEncode) {
                encodedValue = JSONObject.quote(encodedValue)
                encodedValue = value.substring(1, encodedValue.length - 1)
            }
            if (variable.urlEncode) {
                try {
                    encodedValue = URLEncoder.encode(encodedValue, "utf-8")
                } catch (e: UnsupportedEncodingException) {
                    // what kind of stupid system does not support utf-8?!
                }
            }
            resolvedVariables.variableValues.put(variable.key!!, encodedValue)
            return this
        }

        fun build() = resolvedVariables

    }

}
