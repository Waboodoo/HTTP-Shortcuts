package ch.rmy.android.http_shortcuts.data.realm.migration

import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.tryOrLog
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
            tryOrLog {
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
        }

        // 1.16.2
        if (oldVersion < 9) {
            tryOrLog {
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
        }

        // 1.17.0
        if (oldVersion < 10) {
            tryOrLog {
                migrationContext.enumerate("Shortcut") { oldShortcut, newShortcut ->
                    if (oldShortcut.getNullableValue<String>("authentication") == null) {
                        newShortcut?.set("authentication", "none")
                    }
                }
            }
        }

        // 1.19.0
        if (oldVersion < 14) {
            tryOrLog {
                migrationContext.enumerate("Category") { _, newCategory ->
                    newCategory?.set("layoutType", "linear_list")
                }
                migrationContext.enumerate("Option") { _, newOption ->
                    newOption?.set("id", UUIDUtils.newUUID())
                    newOption?.set("label", "")
                    newOption?.set("value", "")
                }
            }
        }

        // 1.20.0
        if (oldVersion < 16) {
            tryOrLog {
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
        }

        // 1.21.0
        if (oldVersion < 18) {
            tryOrLog {
                migrationContext.enumerate("Shortcut") { _, newShortcut ->
                    newShortcut?.set("executionType", "app")
                }
            }
        }

        // 1.24.0
        if (oldVersion < 22) {
            tryOrLog {
                migrationContext.enumerate("Shortcut") { oldShortcut, newShortcut ->
                    newShortcut?.set("id", oldShortcut.getValue<Int>("id").toString())
                }
                migrationContext.enumerate("Category") { oldCategory, newCategory ->
                    newCategory?.set("id", oldCategory.getValue<Int>("id").toString())
                }
                migrationContext.enumerate("Variable") { oldVariable, newVariable ->
                    newVariable?.set("id", oldVariable.getValue<Int>("id").toString())
                }
            }
        }

        // 1.24.0
        if (oldVersion < 23) {
            tryOrLog {
                migrationContext.enumerate("Category") { _, newCategory ->
                    newCategory?.set("background", "white")
                }
            }
        }

        // 1.24.0
        if (oldVersion < 24) {
            tryOrLog {
                migrationContext.enumerate("Shortcut") { _, newShortcut ->
                    newShortcut?.set("followRedirects", true)
                }
            }
        }

        // 1.24.0
        if (oldVersion < 25) {
            tryOrLog {
                ReplaceVariableKeysWithIdsMigration().migrateRealm(migrationContext)
            }
        }

        // 1.24.0
        if (oldVersion in 17..26) {
            tryOrLog {
                ReplaceActionsWithScriptsMigration().migrateRealm(migrationContext)
            }
        }

        // 1.28.0
        if (oldVersion < 33) {
            tryOrLog {
                RemoveLegacyActionsMigration().migrateRealm(migrationContext)
            }
        }

        // 1.29.0
        if (oldVersion < 34) {
            tryOrLog {
                ParameterTypeMigration().migrateRealm(migrationContext)
            }
        }

        // 1.30.0
        if (oldVersion == 36L) {
            tryOrLog {
                migrationContext.enumerate("Widget") { _, newWidget ->
                    newWidget?.set("showLabel", true)
                }
            }
        }

        // 1.32.1
        if (oldVersion < 39) {
            tryOrLog {
                migrationContext.enumerate("Shortcut") { oldShortcut, newShortcut ->
                    val executionType = oldShortcut.getNullableValue<String>("executionType")
                    if (executionType !in setOf("app", "browser", "scripting", "trigger")) {
                        newShortcut?.set("executionType", "app")
                    }
                }
            }
        }

        // 1.35.0
        if (oldVersion < 40) {
            tryOrLog {
                ResponseHandlingMigration().migrateRealm(migrationContext)
            }
        }

        // 2.4.0
        if (oldVersion == 44L) {
            tryOrLog {
                migrationContext.enumerate("Shortcut") { oldShortcut, newShortcut ->
                    val clientCertAlias = oldShortcut.getString("clientCertAlias")
                    if (!clientCertAlias.isNullOrEmpty()) {
                        newShortcut?.set("clientCert", "alias:$clientCertAlias")
                    }
                }
            }
        }

        // 2.15.0
        if (oldVersion in 23..49) {
            tryOrLog {
                CategoryBackgroundMigration().migrateRealm(migrationContext)
            }
        }

        // 2.15.0
        if (oldVersion < 51) {
            tryOrLog {
                CategoryLayoutMigration().migrateRealm(migrationContext)
            }
        }

        // 2.23.0
        if (oldVersion in 40..52) {
            tryOrLog {
                ResponseActionMigration().migrateRealm(migrationContext)
            }
        }

        // 2.26.0
        if (oldVersion < 54) {
            tryOrLog {
                migrationContext.enumerate("Shortcut") { _, newShortcut ->
                    newShortcut?.set("proxy", "HTTP")
                }
            }
        }

        if (oldVersion < 65) {
            tryOrLog {
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
        }

        if (oldVersion < 66) {
            tryOrLog {
                // Some users somehow managed to have duplicate variable IDs. Let's try to correct that
                UniqueIdsMigration().migrateRealm(migrationContext)
            }
        }

        if (oldVersion < 68) {
            tryOrLog {
                RequireConfirmationMigration().migrateRealm(migrationContext)
            }
        }

        if (oldVersion < 71) {
            tryOrLog {
                FileUploadTypeMigration().migrateRealm(migrationContext)
            }
        }

        if (oldVersion in (58 until 78)) {
            tryOrLog {
                WorkingDirectoryMigration().migrateRealm(migrationContext)
            }
        }

        if (oldVersion < 80 && oldVersion >= 36) {
            tryOrLog {
                migrationContext.enumerate("Widget") { _, newWidget ->
                    newWidget?.set("showIcon", true)
                }
            }
        }

        if (oldVersion < 84 && oldVersion >= 36) {
            tryOrLog {
                migrationContext.enumerate("Widget") { _, newWidget ->
                    newWidget?.set("iconScale", 1f)
                }
            }
        }
    }
}
