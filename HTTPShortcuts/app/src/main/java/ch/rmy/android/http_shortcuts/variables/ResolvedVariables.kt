package ch.rmy.android.http_shortcuts.variables

import ch.rmy.android.http_shortcuts.realm.models.ResolvedVariable
import ch.rmy.android.http_shortcuts.realm.models.Variable
import ch.rmy.android.http_shortcuts.utils.mapIf
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

class ResolvedVariables {

    private val variableValues = mutableMapOf<String, String>()

    fun hasValue(variableName: String) = variableValues.containsKey(variableName)

    fun getValue(variableName: String): String? = variableValues[variableName]

    fun toList() = variableValues.map { (key, value) ->
        ResolvedVariable.createNew(key, value)
    }

    class Builder {

        private val resolvedVariables: ResolvedVariables = ResolvedVariables()

        fun add(variable: Variable, value: String) = this.also {
            val encodedValue = value
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
            resolvedVariables.variableValues.put(variable.key, encodedValue)
        }

        fun build() = resolvedVariables

    }

}
