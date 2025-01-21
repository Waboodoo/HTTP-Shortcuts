package ch.rmy.android.http_shortcuts.data.migration.migrations

import ch.rmy.android.framework.utils.UUIDUtils
import ch.rmy.android.http_shortcuts.data.enums.ResponseUiType
import ch.rmy.android.http_shortcuts.data.migration.getObjectArray
import ch.rmy.android.http_shortcuts.data.migration.getString
import ch.rmy.android.http_shortcuts.data.models.ResponseHandling
import com.google.gson.JsonObject
import io.realm.kotlin.migration.AutomaticSchemaMigration

class ResponseHandlingMigration : BaseMigration {

    override fun migrateRealm(migrationContext: AutomaticSchemaMigration.MigrationContext) {
        migrationContext.enumerate("Shortcut") { oldShortcut, newShortcut ->
            if (oldShortcut.getString("executionType") != "app") {
                return@enumerate
            }
            val responseHandling = when (oldShortcut.getString("feedback")) {
                "simple_response" -> ResponseHandling(
                    responseUiType = ResponseUiType.TOAST,
                    successOutput = "message",
                    failureOutput = "simple",
                    successMessage = "",
                    includeMetaInfo = false,
                )
                "simple_response_errors" -> ResponseHandling(
                    responseUiType = ResponseUiType.TOAST,
                    successOutput = "none",
                    failureOutput = "simple",
                    successMessage = "",
                    includeMetaInfo = false,
                )
                "full_response" -> ResponseHandling(
                    responseUiType = ResponseUiType.TOAST,
                    successOutput = "response",
                    failureOutput = "detailed",
                    successMessage = "",
                    includeMetaInfo = false,
                )
                "errors_only" -> ResponseHandling(
                    responseUiType = ResponseUiType.TOAST,
                    successOutput = "none",
                    failureOutput = "detailed",
                    successMessage = "",
                    includeMetaInfo = false,
                )
                "dialog" -> ResponseHandling(
                    responseUiType = ResponseUiType.DIALOG,
                    successOutput = "response",
                    failureOutput = "detailed",
                    successMessage = "",
                    includeMetaInfo = false,
                )
                "activity" -> ResponseHandling(
                    responseUiType = ResponseUiType.WINDOW,
                    successOutput = "response",
                    failureOutput = "detailed",
                    successMessage = "",
                    includeMetaInfo = false,
                )
                "debug" -> ResponseHandling(
                    responseUiType = ResponseUiType.WINDOW,
                    successOutput = "response",
                    failureOutput = "detailed",
                    successMessage = "",
                    includeMetaInfo = true,
                )
                else -> ResponseHandling(
                    responseUiType = ResponseUiType.TOAST,
                    successOutput = "none",
                    failureOutput = "none",
                    successMessage = "",
                    includeMetaInfo = false,
                )
            }
            newShortcut?.set("responseHandling", responseHandling)
        }
    }

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
