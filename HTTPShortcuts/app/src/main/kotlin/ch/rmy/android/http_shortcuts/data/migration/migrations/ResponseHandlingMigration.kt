package ch.rmy.android.http_shortcuts.data.migration.migrations

import ch.rmy.android.http_shortcuts.utils.UUIDUtils
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.realm.DynamicRealm

class ResponseHandlingMigration : BaseMigration {

    override val version: Int = 40

    override fun migrateRealm(realm: DynamicRealm) {
        val schema = realm.schema
        val responseHandlingSchema = schema.createWithPrimaryKeyField(
            "ResponseHandling",
            "id",
            String::class.java
        )
            .setRequired("id", true)
            .addField("uiType", String::class.java)
            .setRequired("uiType", true)
            .addField("successOutput", String::class.java)
            .setRequired("successOutput", true)
            .addField("failureOutput", String::class.java)
            .setRequired("failureOutput", true)
            .addField("successMessage", String::class.java)
            .setRequired("successMessage", true)
            .addField("includeMetaInfo", Boolean::class.javaPrimitiveType!!)

        val shortcutSchema = schema.get("Shortcut")!!
            .addRealmObjectField("responseHandling", responseHandlingSchema)

        realm.where("Shortcut").findAll().forEach { shortcut ->
            if (shortcut.getString("executionType") == "app") {
                realm.createObject("ResponseHandling", UUIDUtils.newUUID())
                    .apply {
                        when (shortcut.getString("feedback")) {
                            "simple_response" -> {
                                setString("uiType", "toast")
                                setString("successOutput", "message")
                                setString("failureOutput", "simple")
                                setString("successMessage", "")
                                setBoolean("includeMetaInfo", false)
                            }
                            "simple_response_errors" -> {
                                setString("uiType", "toast")
                                setString("successOutput", "none")
                                setString("failureOutput", "simple")
                                setString("successMessage", "")
                                setBoolean("includeMetaInfo", false)
                            }
                            "full_response" -> {
                                setString("uiType", "toast")
                                setString("successOutput", "response")
                                setString("failureOutput", "detailed")
                                setString("successMessage", "")
                                setBoolean("includeMetaInfo", false)
                            }
                            "errors_only" -> {
                                setString("uiType", "toast")
                                setString("successOutput", "none")
                                setString("failureOutput", "detailed")
                                setString("successMessage", "")
                                setBoolean("includeMetaInfo", false)
                            }
                            "dialog" -> {
                                setString("uiType", "dialog")
                                setString("successOutput", "response")
                                setString("failureOutput", "detailed")
                                setString("successMessage", "")
                                setBoolean("includeMetaInfo", false)
                            }
                            "activity" -> {
                                setString("uiType", "window")
                                setString("successOutput", "response")
                                setString("failureOutput", "detailed")
                                setString("successMessage", "")
                                setBoolean("includeMetaInfo", false)
                            }
                            "debug" -> {
                                setString("uiType", "window")
                                setString("successOutput", "response")
                                setString("failureOutput", "detailed")
                                setString("successMessage", "")
                                setBoolean("includeMetaInfo", true)
                            }
                            else -> {
                                setString("uiType", "toast")
                                setString("successOutput", "none")
                                setString("failureOutput", "none")
                                setString("successMessage", "")
                                setBoolean("includeMetaInfo", false)
                            }
                        }
                    }
                    .let { responseHandling ->
                        shortcut.setObject("responseHandling", responseHandling)
                    }
            }
        }

        shortcutSchema.removeField("feedback")
    }

    override fun migrateImport(base: JsonObject) {
        for (category in base["categories"].asJsonArray) {
            for (shortcut in category.asJsonObject["shortcuts"]?.asJsonArray ?: JsonArray()) {
                val shortcutObject = shortcut.asJsonObject
                val executionType = shortcutObject.get("executionType")?.asString
                if (executionType == "app" || executionType == null) {
                    JsonObject()
                        .apply {
                            addProperty("id", UUIDUtils.newUUID())
                            when (shortcutObject.get("feedback")?.asString) {
                                "simple_response" -> {
                                    addProperty("uiType", "toast")
                                    addProperty("successOutput", "message")
                                    addProperty("failureOutput", "simple")
                                    addProperty("successMessage", "")
                                    addProperty("includeMetaInfo", false)
                                }
                                "simple_response_errors" -> {
                                    addProperty("uiType", "toast")
                                    addProperty("successOutput", "none")
                                    addProperty("failureOutput", "simple")
                                    addProperty("successMessage", "")
                                    addProperty("includeMetaInfo", false)
                                }
                                "full_response" -> {
                                    addProperty("uiType", "toast")
                                    addProperty("successOutput", "response")
                                    addProperty("failureOutput", "detailed")
                                    addProperty("successMessage", "")
                                    addProperty("includeMetaInfo", false)
                                }
                                "errors_only" -> {
                                    addProperty("uiType", "toast")
                                    addProperty("successOutput", "none")
                                    addProperty("failureOutput", "detailed")
                                    addProperty("successMessage", "")
                                    addProperty("includeMetaInfo", false)
                                }
                                "dialog" -> {
                                    addProperty("uiType", "dialog")
                                    addProperty("successOutput", "response")
                                    addProperty("failureOutput", "detailed")
                                    addProperty("successMessage", "")
                                    addProperty("includeMetaInfo", false)
                                }
                                "activity" -> {
                                    addProperty("uiType", "window")
                                    addProperty("successOutput", "response")
                                    addProperty("failureOutput", "detailed")
                                    addProperty("successMessage", "")
                                    addProperty("includeMetaInfo", false)
                                }
                                "debug" -> {
                                    addProperty("uiType", "window")
                                    addProperty("successOutput", "response")
                                    addProperty("failureOutput", "detailed")
                                    addProperty("successMessage", "")
                                    addProperty("includeMetaInfo", true)
                                }
                                else -> {
                                    addProperty("uiType", "toast")
                                    addProperty("successOutput", "none")
                                    addProperty("failureOutput", "none")
                                    addProperty("successMessage", "")
                                    addProperty("includeMetaInfo", false)
                                }
                            }
                        }
                        .let { responseHandling ->
                            shortcutObject.add("responseHandling", responseHandling)
                        }
                }
            }
        }
    }
}
