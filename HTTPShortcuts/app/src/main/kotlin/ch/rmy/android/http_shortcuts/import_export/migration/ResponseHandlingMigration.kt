package ch.rmy.android.http_shortcuts.import_export.migration

import ch.rmy.android.framework.utils.UUIDUtils
import ch.rmy.android.http_shortcuts.import_export.getObjectArray
import ch.rmy.android.http_shortcuts.import_export.getString
import com.google.gson.JsonObject

class ResponseHandlingMigration : ImportMigration {
    override fun migrateImport(base: JsonObject) {
        for (category in base.getObjectArray("categories")) {
            for (shortcut in category.getObjectArray("shortcuts")) {
                val executionType = shortcut.getString("executionType")
                if (executionType == "app" || executionType == null) {
                    JsonObject()
                        .apply {
                            addProperty("id", UUIDUtils.newUUID())
                            when (shortcut.getString("feedback")) {
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
                            shortcut.add("responseHandling", responseHandling)
                        }
                }
            }
        }
    }
}
