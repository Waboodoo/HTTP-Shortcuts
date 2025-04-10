package ch.rmy.android.http_shortcuts.import_export.migration

import ch.rmy.android.http_shortcuts.import_export.getObjectArray
import ch.rmy.android.http_shortcuts.import_export.getString
import com.google.gson.JsonObject

class ReplaceVariableKeysWithIdsMigration : ImportMigration {
    override fun migrateImport(base: JsonObject) {
        val variableMap = base.getObjectArray("variables")
            .associate { it.getString("key")!! to it.getString("id")!! }

        base.getObjectArray("categories")
            .flatMap { it.getObjectArray("shortcuts") }
            .forEach { shortcut ->
                migrateField(shortcut, "url", variableMap)
                migrateField(shortcut, "username", variableMap)
                migrateField(shortcut, "password", variableMap)
                migrateField(shortcut, "bodyContent", variableMap)
                migrateField(shortcut, "serializedBeforeActions", variableMap)
                migrateField(shortcut, "serializedSuccessActions", variableMap)
                migrateField(shortcut, "serializedFailureActions", variableMap)

                shortcut.getObjectArray("parameters")
                    .forEach { parameter ->
                        migrateField(parameter, "key", variableMap)
                        migrateField(parameter, "value", variableMap)
                    }
                shortcut.getObjectArray("headers")
                    .forEach { header ->
                        migrateField(header, "key", variableMap)
                        migrateField(header, "value", variableMap)
                    }
            }

        base.getObjectArray("variables")
            .forEach { variable ->
                migrateField(variable, "value", variableMap)
                migrateField(variable, "data", variableMap)

                variable.getObjectArray("options")
                    .forEach { option ->
                        migrateField(option, "value", variableMap)
                    }
            }
    }

    private fun migrateField(obj: JsonObject, field: String, variableMap: Map<String, String>) {
        val value = obj.getString(field) ?: return
        obj.addProperty(field, replaceVariables(value, variableMap))
    }

    private fun replaceVariables(string: String, variableMap: Map<String, String>): String =
        string
            .replace(PLACEHOLDER_REGEX) { match ->
                val variableKey = match.groups[1]!!.value
                "{{" + (variableMap[variableKey] ?: variableKey) + "}}"
            }
            .replace(JSON_PLACEHOLDER_REGEX) { match ->
                val variableKey = match.groups[1]!!.value
                "\\{\\{" + (variableMap[variableKey] ?: variableKey) + "\\}\\}"
            }
            .replace(VARIABLE_KEY_JSON_REGEX) { match ->
                val variableKey = match.groups[1]!!.value
                "\"variableId\":\"" + (variableMap[variableKey] ?: variableKey) + "\""
            }

    companion object {

        private const val VARIABLE_KEY_REGEX = "[A-Za-z0-9_]{1,30}"
        private val PLACEHOLDER_REGEX = ("\\{\\{($VARIABLE_KEY_REGEX)\\}\\}").toRegex()
        private val JSON_PLACEHOLDER_REGEX = ("\\\\\\{\\\\\\{($VARIABLE_KEY_REGEX)\\\\\\}\\\\\\}").toRegex()
        private val VARIABLE_KEY_JSON_REGEX = ("\\\"variableKey\\\":\\\"($VARIABLE_KEY_REGEX)\\\"").toRegex()
    }
}
