package ch.rmy.android.http_shortcuts.data.migration.migrations

import ch.rmy.android.http_shortcuts.data.migration.getString
import ch.rmy.android.http_shortcuts.extensions.getArrayOrEmpty
import com.google.gson.JsonObject
import io.realm.kotlin.dynamic.DynamicMutableRealmObject
import io.realm.kotlin.dynamic.DynamicRealmObject
import io.realm.kotlin.dynamic.getNullableValue
import io.realm.kotlin.migration.AutomaticSchemaMigration

@Suppress("ALL")
class ReplaceVariableKeysWithIdsMigration : BaseMigration {
    override fun migrateRealm(migrationContext: AutomaticSchemaMigration.MigrationContext) {
        val oldRealm = migrationContext.oldRealm
        val oldVersion = oldRealm.schemaVersion()

        val variableMap = oldRealm.query("Variable")
            .find()
            .associate { variable ->
                variable.getString("key").orEmpty() to variable.getString("id").orEmpty()
            }

        migrationContext.enumerate("Shortcut") { oldShortcut, newShortcut ->
            migrateField(oldShortcut, newShortcut, "url", variableMap)
            migrateField(oldShortcut, newShortcut, "username", variableMap)
            migrateField(oldShortcut, newShortcut, "password", variableMap)
            migrateField(oldShortcut, newShortcut, "bodyContent", variableMap)
            if (oldVersion >= 17) {
                migrateField(oldShortcut, newShortcut, "serializedBeforeActions", variableMap)
                migrateField(oldShortcut, newShortcut, "serializedSuccessActions", variableMap)
                migrateField(oldShortcut, newShortcut, "serializedFailureActions", variableMap)
            }
        }

        migrationContext.enumerate("Parameter") { oldParameter, newParameter ->
            migrateField(oldParameter, newParameter, "key", variableMap)
            migrateField(oldParameter, newParameter, "value", variableMap)
        }

        migrationContext.enumerate("Header") { oldHeader, newHeader ->
            migrateField(oldHeader, newHeader, "key", variableMap)
            migrateField(oldHeader, newHeader, "value", variableMap)
        }

        migrationContext.enumerate("Variable") { oldVariable, newVariable ->
            migrateField(oldVariable, newVariable, "value", variableMap)
            migrateField(oldVariable, newVariable, "data", variableMap)
        }

        migrationContext.enumerate("Option") { oldOption, newOption ->
            migrateField(oldOption, newOption, "value", variableMap)
        }
    }

    private fun migrateField(oldObject: DynamicRealmObject, newObject: DynamicMutableRealmObject?, field: String, variableMap: Map<String, String>) {
        val oldValue = oldObject.getNullableValue<String>(field) ?: return
        newObject?.set(field, replaceVariables(oldValue, variableMap))
    }

    override fun migrateImport(base: JsonObject) {
        val variableMap = base.getArrayOrEmpty("variables")
            .map { it.asJsonObject }
            .associate { it.get("key").asString!! to it.get("id").asString!! }

        base.getArrayOrEmpty("categories")
            .map { it.asJsonObject }
            .flatMap { it.getArrayOrEmpty("shortcuts").asJsonArray }
            .map { it.asJsonObject }
            .forEach { shortcut ->
                migrateField(shortcut, "url", variableMap)
                migrateField(shortcut, "username", variableMap)
                migrateField(shortcut, "password", variableMap)
                migrateField(shortcut, "bodyContent", variableMap)
                migrateField(shortcut, "serializedBeforeActions", variableMap)
                migrateField(shortcut, "serializedSuccessActions", variableMap)
                migrateField(shortcut, "serializedFailureActions", variableMap)

                shortcut.get("parameters")
                    ?.takeUnless { it.isJsonNull }
                    ?.asJsonArray
                    ?.map { it.asJsonObject }
                    ?.forEach { parameter ->
                        migrateField(parameter, "key", variableMap)
                        migrateField(parameter, "value", variableMap)
                    }
                shortcut.get("headers")
                    ?.takeUnless { it.isJsonNull }
                    ?.asJsonArray
                    ?.map { it.asJsonObject }
                    ?.forEach { parameter ->
                        migrateField(parameter, "key", variableMap)
                        migrateField(parameter, "value", variableMap)
                    }
            }

        base.getArrayOrEmpty("variables")
            .map { it.asJsonObject }
            .forEach { variable ->
                migrateField(variable, "value", variableMap)
                migrateField(variable, "data", variableMap)

                variable.get("options")
                    ?.takeUnless { it.isJsonNull }
                    ?.asJsonArray
                    ?.map { it.asJsonObject }
                    ?.forEach { option ->
                        migrateField(option, "value", variableMap)
                    }
            }
    }

    private fun migrateField(obj: JsonObject, field: String, variableMap: Map<String, String>) {
        val value = obj.get(field)?.takeUnless { it.isJsonNull } ?: return
        obj.addProperty(field, replaceVariables(value.asString, variableMap))
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
