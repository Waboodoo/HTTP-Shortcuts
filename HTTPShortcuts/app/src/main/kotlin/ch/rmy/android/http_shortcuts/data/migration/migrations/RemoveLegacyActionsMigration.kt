package ch.rmy.android.http_shortcuts.data.migration.migrations

import ch.rmy.android.framework.extensions.logException
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.realm.DynamicRealm
import io.realm.DynamicRealmObject

class RemoveLegacyActionsMigration : BaseMigration {

    override val version: Int = 33

    private val pattern = "_runAction\\(\"([a-z_]+)\", (\\{.+\\})\\); /\\* built-in \\*/".toPattern()

    override fun migrateImport(base: JsonObject) {
        base.getAsJsonArray("categories")
            .map { it.asJsonObject }
            .flatMap { it.getAsJsonArray("shortcuts").asJsonArray }
            .map { it.asJsonObject }
            .forEach { shortcut ->
                migrateField(shortcut, "codeOnPrepare")
                migrateField(shortcut, "codeOnSuccess")
                migrateField(shortcut, "codeOnFailure")
            }
    }

    private fun migrateField(obj: JsonObject, field: String) {
        val value = obj.get(field)?.takeUnless { it.isJsonNull } ?: return
        obj.addProperty(field, migrateScript(value.asString))
    }

    override fun migrateRealm(realm: DynamicRealm) {
        realm.where("Shortcut")
            .findAll()
            .forEach { shortcut ->
                migrateField(shortcut, "codeOnPrepare")
                migrateField(shortcut, "codeOnSuccess")
                migrateField(shortcut, "codeOnFailure")
            }
    }

    private fun migrateField(shortcut: DynamicRealmObject, fieldName: String) {
        val script = shortcut.getString(fieldName)
        shortcut.setString(fieldName, migrateScript(script))
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
        val variableId = payload.get("variableId").asString
        return when (payload.get("extractionType").asString) {
            "full_body" -> "setVariable(\"$variableId\", response.body);"
            "substring" -> {
                val substringStart = payload.get("substringStart").asString
                val substringEnd = payload.get("substringEnd").asString

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
                val jsonPath = payload.get("jsonPath").asString
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
        val cookieName = payload.get("cookieName").asString
        val variableId = payload.get("variableId").asString
        return "setVariable(\"$variableId\", response.cookies[\"$cookieName\"]);"
    }

    private fun migrateExtractHeader(payload: JsonObject): String {
        val headerKey = payload.get("headerKey").asString
        val variableId = payload.get("variableId").asString
        return "setVariable(\"$variableId\", response.headers[\"$headerKey\"]);"
    }

    private fun migrateExtractStatusCode(payload: JsonObject): String {
        val variableId = payload.get("variableId").asString
        return "setVariable(\"$variableId\", response.statusCode);"
    }

    private fun migrateSetVariable(payload: JsonObject): String {
        val variableId = payload.get("variableId").asString
        val value = payload.get("newValue").asString
        return "setVariable(\"$variableId\", \"$value\");"
    }
}
