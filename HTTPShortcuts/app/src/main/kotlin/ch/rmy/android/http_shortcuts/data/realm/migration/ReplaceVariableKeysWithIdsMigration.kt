package ch.rmy.android.http_shortcuts.data.realm.migration

import io.realm.kotlin.dynamic.DynamicMutableRealmObject
import io.realm.kotlin.dynamic.DynamicRealmObject
import io.realm.kotlin.dynamic.getNullableValue
import io.realm.kotlin.migration.AutomaticSchemaMigration

class ReplaceVariableKeysWithIdsMigration : RealmMigration {
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
