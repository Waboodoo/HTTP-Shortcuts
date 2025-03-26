package ch.rmy.android.http_shortcuts.data.realm.migration

import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.http_shortcuts.import_export.getString
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.realm.kotlin.dynamic.DynamicMutableRealmObject
import io.realm.kotlin.dynamic.DynamicRealmObject
import io.realm.kotlin.migration.AutomaticSchemaMigration

class RemoveLegacyActionsMigration : RealmMigration {
    private val pattern = "_runAction\\(\"([a-z_]+)\", (\\{.+\\})\\); /\\* built-in \\*/".toPattern()

    override fun migrateRealm(migrationContext: AutomaticSchemaMigration.MigrationContext) {
        migrationContext.enumerate("Shortcut") { oldShortcut, newShortcut ->
            migrateField(oldShortcut, newShortcut, "codeOnPrepare")
            migrateField(oldShortcut, newShortcut, "codeOnSuccess")
            migrateField(oldShortcut, newShortcut, "codeOnFailure")
        }
    }

    private fun migrateField(oldShortcut: DynamicRealmObject, newShortcut: DynamicMutableRealmObject?, fieldName: String) {
        val script = oldShortcut.getString(fieldName) ?: ""
        newShortcut?.set(fieldName, migrateScript(script))
    }

    private fun migrateScript(script: String): String {
        var index = 99
        return pattern.toRegex().replace(script) { matchResult ->
            try {
                index++
                val actionType = matchResult.groupValues[1]
                val payload = JsonParser.parseString(matchResult.groupValues[2]).asJsonObject
                when (actionType) {
                    "extract_body" -> migrateExtractBody(payload, index)
                    "extract_cookie" -> migrateExtractCookie(payload)
                    "extract_header" -> migrateExtractHeader(payload)
                    "extract_status_code" -> migrateExtractStatusCode(payload)
                    "set_variable" -> migrateSetVariable(payload)
                    else -> matchResult.value
                }
            } catch (e: Exception) {
                logException(e)
                matchResult.value
            }
        }
    }

    private fun migrateExtractBody(payload: JsonObject, i: Int): String {
        val variableId = payload.getString("variableId")
        return when (payload.getString("extractionType")) {
            "full_body" -> "setVariable(\"$variableId\", response.body);"
            "substring" -> {
                val substringStart = payload.getString("substringStart")
                val substringEnd = payload.getString("substringEnd")

                """
                    let start$i = $substringStart;
                    let end$i = $substringEnd;
                    if (start$i < 0) {
                        start$i += response.body.length;
                    }
                    if (end$i <= 0) {
                        end$i += response.body.length;
                    }
                    setVariable("$variableId", response.body.substring(start$i, end$i));
                """.trimIndent()
            }
            "parse_json" -> {
                val jsonPath = payload.getString("jsonPath") ?: ""
                if (jsonPath.isEmpty()) {
                    "setVariable(\"$variableId\", response.body);"
                } else {
                    val selector = jsonPath.split('.')
                        .joinToString(separator = "") { key ->
                            key.toIntOrNull()?.let {
                                "[$key]"
                            } ?: "[\"$key\"]"
                        }
                    "const json$i = JSON.parse(response.body);\nsetVariable(\"$variableId\", json$i$selector);"
                }
            }
            else -> throw Exception()
        }
    }

    private fun migrateExtractCookie(payload: JsonObject): String {
        val cookieName = payload.getString("cookieName")
        val variableId = payload.getString("variableId")
        return "setVariable(\"$variableId\", response.cookies[\"$cookieName\"]);"
    }

    private fun migrateExtractHeader(payload: JsonObject): String {
        val headerKey = payload.getString("headerKey")
        val variableId = payload.getString("variableId")
        return "setVariable(\"$variableId\", response.headers[\"$headerKey\"]);"
    }

    private fun migrateExtractStatusCode(payload: JsonObject): String {
        val variableId = payload.getString("variableId")
        return "setVariable(\"$variableId\", response.statusCode);"
    }

    private fun migrateSetVariable(payload: JsonObject): String {
        val variableId = payload.getString("variableId")
        val value = payload.getString("newValue")
        return "setVariable(\"$variableId\", \"$value\");"
    }
}
