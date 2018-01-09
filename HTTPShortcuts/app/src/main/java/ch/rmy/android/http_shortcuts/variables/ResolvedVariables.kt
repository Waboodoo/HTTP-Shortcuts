package ch.rmy.android.http_shortcuts.variables

import ch.rmy.android.http_shortcuts.realm.models.ResolvedVariable
import ch.rmy.android.http_shortcuts.realm.models.Variable
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.*

class ResolvedVariables {

    private val variableValues = HashMap<String, String>()

    fun hasValue(variableName: String) = variableValues.containsKey(variableName)

    fun getValue(variableName: String): String? = variableValues[variableName]

    fun toList() = variableValues.map { (key, value) ->
        ResolvedVariable.createNew(key, value)
    }

    class Builder {

        private val resolvedVariables: ResolvedVariables = ResolvedVariables()

        fun add(variable: Variable, value: String) = this.also {
            var encodedValue = value
            if (variable.jsonEncode) {
                encodedValue = JSONObject.quote(encodedValue)
                encodedValue = encodedValue.substring(1, encodedValue.length - 1)
            }
            if (variable.urlEncode) {
                try {
                    encodedValue = URLEncoder.encode(encodedValue, "utf-8")
                } catch (e: UnsupportedEncodingException) {
                    // what kind of stupid system does not support utf-8?!
                }
            }
            resolvedVariables.variableValues.put(variable.key!!, encodedValue)
        }

        fun build() = resolvedVariables

    }

}
