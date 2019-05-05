package ch.rmy.android.http_shortcuts.data.migration.migrations

import com.google.gson.JsonObject
import io.realm.DynamicRealm
import io.realm.DynamicRealmObject

class ReplaceVariableKeysWithIdsMigration : BaseMigration {

    override val version = 25

    override fun migrateRealm(realm: DynamicRealm) {
        val variableMap = realm.where("Variable")
            .findAll()
            .associate { variable ->
                variable.getString("key") to variable.getString("id")
            }

        realm.where("Shortcut")
            .findAll()
            .forEach { shortcut ->
                migrateField(shortcut, "url", variableMap)
                migrateField(shortcut, "username", variableMap)
                migrateField(shortcut, "password", variableMap)
                migrateField(shortcut, "bodyContent", variableMap)
                migrateField(shortcut, "serializedBeforeActions", variableMap)
                migrateField(shortcut, "serializedSuccessActions", variableMap)
                migrateField(shortcut, "serializedFailureActions", variableMap)
            }
        realm.where("Parameter")
            .findAll()
            .forEach { parameter ->
                migrateField(parameter, "key", variableMap)
                migrateField(parameter, "value", variableMap)
            }
        realm.where("Header")
            .findAll()
            .forEach { parameter ->
                migrateField(parameter, "key", variableMap)
                migrateField(parameter, "value", variableMap)
            }
        realm.where("Variable")
            .findAll()
            .forEach { variable ->
                migrateField(variable, "value", variableMap)
                migrateField(variable, "data", variableMap)
            }
        realm.where("Option")
            .findAll()
            .forEach { option ->
                migrateField(option, "value", variableMap)
            }
    }

    private fun migrateField(obj: DynamicRealmObject, field: String, variableMap: Map<String, String>) {
        if (obj.isNull(field)) {
            return
        }
        obj.setString(field, replaceVariables(obj.getString(field), variableMap))
    }

    override fun migrateImport(base: JsonObject) {
        val variableMap = base.getAsJsonArray("variables")
            .map { it.asJsonObject }
            .associate { it.get("key").asString!! to it.get("id").asString!! }

        base.getAsJsonArray("categories")
            .map { it.asJsonObject }
            .flatMap { it.getAsJsonArray("shortcuts").asJsonArray }
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

        base.getAsJsonArray("variables")
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
        private val PLACEHOLDER_REGEX = ("\\{\\{(" + VARIABLE_KEY_REGEX + ")\\}\\}").toRegex()
        private val JSON_PLACEHOLDER_REGEX = ("\\\\\\{\\\\\\{(" + VARIABLE_KEY_REGEX + ")\\\\\\}\\\\\\}").toRegex()
        private val VARIABLE_KEY_JSON_REGEX = ("\\\"variableKey\\\":\\\"(" + VARIABLE_KEY_REGEX + ")\\\"").toRegex()

    }

}