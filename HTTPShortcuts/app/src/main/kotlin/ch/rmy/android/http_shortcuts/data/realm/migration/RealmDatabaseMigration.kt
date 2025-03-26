package ch.rmy.android.http_shortcuts.data.realm.migration

import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.utils.UUIDUtils
import io.realm.kotlin.dynamic.getNullableValue
import io.realm.kotlin.dynamic.getValue
import io.realm.kotlin.migration.AutomaticSchemaMigration

class RealmDatabaseMigration : AutomaticSchemaMigration {
    override fun migrate(migrationContext: AutomaticSchemaMigration.MigrationContext) {
        val oldRealm = migrationContext.oldRealm
        val oldVersion = oldRealm.schemaVersion()
        val newRealm = migrationContext.newRealm

        logInfo("Beginning Realm migration from version $oldVersion")

        // 1.16.0
        if (oldVersion < 6) {
            migrationContext.enumerate("Shortcut") { oldShortcut, newShortcut ->
                newShortcut?.let {
                    if (
                        !oldShortcut.getNullableValue<String>("username").isNullOrEmpty() ||
                        !oldShortcut.getNullableValue<String>("password").isNullOrEmpty()
                    ) {
                        newShortcut.set("authentication", "basic")
                    }
                }
            }
        }

        // 1.16.2
        if (oldVersion < 9) {
            migrationContext.enumerate("Parameter") { _, newParameter ->
                newParameter?.set("id", UUIDUtils.newUUID())
            }
            migrationContext.enumerate("Header") { _, newHeader ->
                newHeader?.set("id", UUIDUtils.newUUID())
            }
            migrationContext.enumerate("Option") { _, newOption ->
                newOption?.set("id", UUIDUtils.newUUID())
            }
        }

        // 1.17.0
        if (oldVersion < 10) {
            migrationContext.enumerate("Shortcut") { oldShortcut, newShortcut ->
                if (oldShortcut.getNullableValue<String>("authentication") == null) {
                    newShortcut?.set("authentication", "none")
                }
            }
        }

        // 1.19.0
        if (oldVersion < 14) {
            migrationContext.enumerate("Category") { _, newCategory ->
                newCategory?.set("layoutType", "linear_list")
            }
            migrationContext.enumerate("Option") { _, newOption ->
                newOption?.set("id", UUIDUtils.newUUID())
                newOption?.set("label", "")
                newOption?.set("value", "")
            }
            migrationContext.enumerate("ResolvedVariable") { _, newResolvedVariable ->
                newResolvedVariable?.set("key", "")
                newResolvedVariable?.set("value", "")
            }
        }

        // 1.20.0
        if (oldVersion < 16) {
            migrationContext.enumerate("Shortcut") { oldShortcut, newShortcut ->
                newShortcut?.set(
                    "requestBodyType",
                    if (oldShortcut.getObjectList("parameters").isEmpty()) {
                        "custom_text"
                    } else {
                        "x_www_form_urlencode"
                    },
                )
                newShortcut?.set("contentType", "text/plain")
            }
        }

        // 1.21.0
        if (oldVersion < 18) {
            migrationContext.enumerate("Shortcut") { _, newShortcut ->
                newShortcut?.set("executionType", "app")
            }
        }

        // 1.24.0
        if (oldVersion < 22) {
            migrationContext.enumerate("Shortcut") { oldShortcut, newShortcut ->
                newShortcut?.set("id", oldShortcut.getValue<Int>("id").toString())
            }
            migrationContext.enumerate("Category") { oldCategory, newCategory ->
                newCategory?.set("id", oldCategory.getValue<Int>("id").toString())
            }
            migrationContext.enumerate("Variable") { oldVariable, newVariable ->
                newVariable?.set("id", oldVariable.getValue<Int>("id").toString())
            }
            migrationContext.enumerate("PendingExecution") { oldPendingExecution, newPendingExecution ->
                newPendingExecution?.set("id", oldPendingExecution.getValue<Int>("id").toString())
            }
        }

        // 1.24.0
        if (oldVersion < 23) {
            migrationContext.enumerate("Category") { _, newCategory ->
                newCategory?.set("background", "white")
            }
        }

        // 1.24.0
        if (oldVersion < 24) {
            migrationContext.enumerate("Shortcut") { _, newShortcut ->
                newShortcut?.set("followRedirects", true)
            }
        }

        // 1.24.0
        if (oldVersion < 25) {
            ReplaceVariableKeysWithIdsMigration().migrateRealm(migrationContext)
        }

        // 1.24.0
        if (oldVersion in 17..26) {
            ReplaceActionsWithScriptsMigration().migrateRealm(migrationContext)
        }

        // 1.28.0
        if (oldVersion < 33) {
            RemoveLegacyActionsMigration().migrateRealm(migrationContext)
        }

        // 1.29.0
        if (oldVersion < 34) {
            ParameterTypeMigration().migrateRealm(migrationContext)
        }

        // 1.30.0
        if (oldVersion == 36L) {
            migrationContext.enumerate("Widget") { _, newWidget ->
                newWidget?.set("showLabel", true)
            }
        }

        // 1.32.1
        if (oldVersion < 39) {
            migrationContext.enumerate("Shortcut") { oldShortcut, newShortcut ->
                val executionType = oldShortcut.getNullableValue<String>("executionType")
                if (executionType !in setOf("app", "browser", "scripting", "trigger")) {
                    newShortcut?.set("executionType", "app")
                }
            }
        }

        // 1.35.0
        if (oldVersion < 40) {
            ResponseHandlingMigration().migrateRealm(migrationContext)
        }

        // 2.4.0
        if (oldVersion == 44L) {
            migrationContext.enumerate("Shortcut") { oldShortcut, newShortcut ->
                val clientCertAlias = oldShortcut.getString("clientCertAlias")
                if (!clientCertAlias.isNullOrEmpty()) {
                    newShortcut?.set("clientCert", "alias:$clientCertAlias")
                }
            }
        }

        // 2.13.0
        if (oldVersion < 47) {
            migrationContext.enumerate("ResolvedVariable") { _, newResolvedVariable ->
                newResolvedVariable?.set("id", UUIDUtils.newUUID())
            }
        }

        // 2.15.0
        if (oldVersion in 23..49) {
            CategoryBackgroundMigration().migrateRealm(migrationContext)
        }

        // 2.15.0
        if (oldVersion < 51) {
            CategoryLayoutMigration().migrateRealm(migrationContext)
        }

        // 2.23.0
        if (oldVersion in 40..52) {
            ResponseActionMigration().migrateRealm(migrationContext)
        }

        // 2.26.0
        if (oldVersion < 54) {
            migrationContext.enumerate("Shortcut") { _, newShortcut ->
                newShortcut?.set("proxy", "HTTP")
            }
        }

        // 2.32.0
        if (oldVersion < 60) {
            newRealm.query("PendingExecution").find()
                .reversed()
                .forEach {
                    newRealm.delete(it)
                }
            newRealm.query("HistoryEvent").find()
                .reversed()
                .forEach {
                    newRealm.delete(it)
                }
        }

        if (oldVersion < 65) {
            // Abuse the "uiType" field as a marker field to remove orphaned instances of ResponseHandling
            val markerPrefix = "// "
            newRealm.query("Shortcut").find().forEach { shortcut ->
                shortcut.getObject("responseHandling")?.let {
                    val uiType = it.getValue<String>("uiType")
                    it.set("uiType", markerPrefix + uiType)
                }
            }

            newRealm.query("ResponseHandling").find().toList().forEach {
                val uiTypeOrIdPlusUiType = it.getValue<String>("uiType")
                if (uiTypeOrIdPlusUiType.startsWith(markerPrefix)) {
                    it.set("uiType", uiTypeOrIdPlusUiType.removePrefix(markerPrefix))
                } else {
                    logInfo("Deleting an unreachable ResponseHandling object")
                    newRealm.delete(it)
                }
            }
        }

        if (oldVersion < 66) {
            // Some users somehow managed to have duplicate variable IDs. Let's try to correct that
            UniqueIdsMigration().migrateRealm(migrationContext)
        }

        if (oldVersion < 68) {
            RequireConfirmationMigration().migrateRealm(migrationContext)
        }

        if (oldVersion < 71) {
            FileUploadTypeMigration().migrateRealm(migrationContext)
        }

        if (oldVersion in (58 until 78)) {
            WorkingDirectoryMigration().migrateRealm(migrationContext)
        }

        if (oldVersion < 80 && oldVersion >= 36) {
            migrationContext.enumerate("Widget") { _, newWidget ->
                newWidget?.set("showIcon", true)
            }
        }

        if (oldVersion < 84 && oldVersion >= 36) {
            migrationContext.enumerate("Widget") { _, newWidget ->
                newWidget?.set("iconScale", 1f)
            }
        }
    }
}
