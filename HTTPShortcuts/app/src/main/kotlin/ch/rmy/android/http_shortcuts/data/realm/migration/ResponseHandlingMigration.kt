package ch.rmy.android.http_shortcuts.data.realm.migration

import ch.rmy.android.http_shortcuts.data.enums.ResponseUiType
import ch.rmy.android.http_shortcuts.data.realm.models.RealmResponseHandling
import io.realm.kotlin.migration.AutomaticSchemaMigration

class ResponseHandlingMigration : RealmMigration {
    override fun migrateRealm(migrationContext: AutomaticSchemaMigration.MigrationContext) {
        migrationContext.enumerate("Shortcut") { oldShortcut, newShortcut ->
            if (oldShortcut.getString("executionType") != "app") {
                return@enumerate
            }
            val responseHandling = when (oldShortcut.getString("feedback")) {
                "simple_response" -> RealmResponseHandling().apply {
                    uiType = ResponseUiType.TOAST.type
                    successOutput = "message"
                    failureOutput = "simple"
                    successMessage = ""
                    includeMetaInfo = false
                }
                "simple_response_errors" -> RealmResponseHandling().apply {
                    uiType = ResponseUiType.TOAST.type
                    successOutput = "none"
                    failureOutput = "simple"
                    successMessage = ""
                    includeMetaInfo = false
                }
                "full_response" -> RealmResponseHandling().apply {
                    uiType = ResponseUiType.TOAST.type
                    successOutput = "response"
                    failureOutput = "detailed"
                    successMessage = ""
                    includeMetaInfo = false
                }
                "errors_only" -> RealmResponseHandling().apply {
                    uiType = ResponseUiType.TOAST.type
                    successOutput = "none"
                    failureOutput = "detailed"
                    successMessage = ""
                    includeMetaInfo = false
                }
                "dialog" -> RealmResponseHandling().apply {
                    uiType = ResponseUiType.DIALOG.type
                    successOutput = "response"
                    failureOutput = "detailed"
                    successMessage = ""
                    includeMetaInfo = false
                }
                "activity" -> RealmResponseHandling().apply {
                    uiType = ResponseUiType.WINDOW.type
                    successOutput = "response"
                    failureOutput = "detailed"
                    successMessage = ""
                    includeMetaInfo = false
                }
                "debug" -> RealmResponseHandling().apply {
                    uiType = ResponseUiType.WINDOW.type
                    successOutput = "response"
                    failureOutput = "detailed"
                    successMessage = ""
                    includeMetaInfo = true
                }
                else -> RealmResponseHandling().apply {
                    uiType = ResponseUiType.TOAST.type
                    successOutput = "none"
                    failureOutput = "none"
                    successMessage = ""
                    includeMetaInfo = false
                }
            }
            newShortcut?.set("responseHandling", responseHandling)
        }
    }
}
