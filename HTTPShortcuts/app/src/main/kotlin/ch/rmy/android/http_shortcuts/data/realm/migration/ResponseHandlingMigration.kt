package ch.rmy.android.http_shortcuts.data.realm.migration

import ch.rmy.android.http_shortcuts.data.enums.ResponseUiType
import ch.rmy.android.http_shortcuts.data.models.ResponseHandling
import ch.rmy.android.http_shortcuts.data.realm.getString
import io.realm.kotlin.migration.AutomaticSchemaMigration

class ResponseHandlingMigration : RealmMigration {
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
}
