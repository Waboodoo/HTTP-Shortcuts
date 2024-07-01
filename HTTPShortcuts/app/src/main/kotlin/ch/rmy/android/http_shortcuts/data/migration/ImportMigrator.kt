package ch.rmy.android.http_shortcuts.data.migration

import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.data.migration.migrations.CategoryBackgroundMigration
import ch.rmy.android.http_shortcuts.data.migration.migrations.CategoryLayoutMigration
import ch.rmy.android.http_shortcuts.data.migration.migrations.FileUploadTypeMigration
import ch.rmy.android.http_shortcuts.data.migration.migrations.ParameterTypeMigration
import ch.rmy.android.http_shortcuts.data.migration.migrations.RemoveLegacyActionsMigration
import ch.rmy.android.http_shortcuts.data.migration.migrations.ReplaceActionsWithScriptsMigration
import ch.rmy.android.http_shortcuts.data.migration.migrations.ReplaceVariableKeysWithIdsMigration
import ch.rmy.android.http_shortcuts.data.migration.migrations.RequireConfirmationMigration
import ch.rmy.android.http_shortcuts.data.migration.migrations.ResponseActionMigration
import ch.rmy.android.http_shortcuts.data.migration.migrations.ResponseHandlingMigration
import ch.rmy.android.http_shortcuts.data.migration.migrations.WorkingDirectoryMigration
import com.google.gson.JsonElement
import com.google.gson.JsonObject

internal object ImportMigrator {

    fun migrate(importData: JsonElement): JsonElement {
        val base = importData.asJsonObject
        val fromVersion = base["version"]?.takeUnless { it.isJsonNull }
            ?.asLong
            ?: throw InvalidFileException()
        if (fromVersion > DatabaseMigration.VERSION) {
            val compatibilityVersion = base["compatibilityVersion"]?.takeUnless { it.isJsonNull }?.asLong?.takeUnless { it == 0L }
            if (compatibilityVersion == null || compatibilityVersion > DatabaseMigration.VERSION) {
                throw ImportVersionMismatchException()
            }
        }
        require(base.has("categories")) { "Import data doesn't have any categories" }

        for (version in fromVersion + 1..DatabaseMigration.VERSION) {
            migrate(base, version)
            base.addProperty("version", version)
        }
        return base
    }

    private fun migrate(base: JsonObject, newVersion: Long) {
        when (newVersion) {
            5L -> { // 1.16.0
                for (category in base.getObjectArray("categories")) {
                    category.addProperty("layoutType", "linear_list")
                }
            }
            6L -> { // 1.16.0
                for (category in base.getObjectArray("categories")) {
                    for (shortcut in category.getObjectArray("shortcuts")) {
                        val username = shortcut.getString("username")
                        val password = shortcut.getString("password")
                        if (!username.isNullOrEmpty() || !password.isNullOrEmpty()) {
                            shortcut.addProperty("authentication", "basic")
                        }
                    }
                }
            }
            9L -> { // 1.16.2
                for (category in base.getObjectArray("categories")) {
                    for (shortcut in category.getObjectArray("shortcuts")) {
                        for (header in shortcut.getObjectArray("headers")) {
                            header.addProperty("id", newUUID())
                        }
                        for (parameter in shortcut.getObjectArray("parameters")) {
                            parameter.addProperty("id", newUUID())
                        }
                    }
                }
                for (variable in base.getObjectArray("variables")) {
                    for (option in variable.getObjectArray("options")) {
                        option.addProperty("id", newUUID())
                    }
                }
            }
            10L -> { // 1.17.0
                for (category in base.getObjectArray("categories")) {
                    for (shortcut in category.getObjectArray("shortcuts")) {
                        if (shortcut["authentication"]?.isJsonNull != false) {
                            shortcut.addProperty("authentication", "none")
                            shortcut.addProperty("contentType", "text/plain")
                        }
                    }
                }
            }
            16L -> { // 1.20.0
                for (category in base.getObjectArray("categories")) {
                    for (shortcut in category.getObjectArray("shortcuts")) {
                        shortcut.addProperty("contentType", "text/plain")
                        shortcut.addProperty(
                            "requestBodyType",
                            if (shortcut.getArray("parameters").size() == 0) {
                                "custom_text"
                            } else {
                                "x_www_form_urlencode"
                            },
                        )
                    }
                }
            }
            18L -> { // 1.21.0
                for (category in base.getObjectArray("categories")) {
                    for (shortcut in category.getObjectArray("shortcuts")) {
                        shortcut.addProperty("executionType", "app")
                    }
                }
            }
            22L -> { // 1.24.0
                for (category in base.getObjectArray("categories")) {
                    val oldCategoryId = category["id"].asLong
                    category.remove("id")
                    category.addProperty("id", oldCategoryId.toString())
                    for (shortcut in category.getObjectArray("shortcuts")) {
                        val oldShortcutId = shortcut["id"].asLong
                        shortcut.remove("id")
                        shortcut.addProperty("id", oldShortcutId.toString())
                    }
                }
                for (variable in base.getObjectArray("variables")) {
                    val oldVariableId = variable["id"].asLong
                    variable.remove("id")
                    variable.addProperty("id", oldVariableId.toString())
                }
            }
            23L -> { // 1.24.0
                for (category in base.getObjectArray("categories")) {
                    category.addProperty("background", "white")
                }
            }
            25L -> { // 1.24.0
                ReplaceVariableKeysWithIdsMigration().migrateImport(base)
            }
            27L -> { // 1.24.0
                ReplaceActionsWithScriptsMigration().migrateImport(base)
            }
            33L -> { // 1.28.0
                RemoveLegacyActionsMigration().migrateImport(base)
            }
            34L -> { // 1.29.0
                ParameterTypeMigration().migrateImport(base)
            }
            40L -> { // 1.35.0
                ResponseHandlingMigration().migrateImport(base)
            }
            45L -> { // 2.4.0
                for (category in base.getObjectArray("categories")) {
                    for (shortcut in category.getObjectArray("shortcuts")) {
                        val clientCertAlias = shortcut.getString("clientCertAlias")
                        if (!clientCertAlias.isNullOrEmpty()) {
                            shortcut.addProperty("clientCert", "alias:$clientCertAlias")
                        }
                    }
                }
            }
            50L -> { // 2.15.0
                CategoryBackgroundMigration().migrateImport(base)
            }
            51L -> { // 2.15.1
                CategoryLayoutMigration().migrateImport(base)
            }
            53L -> { // 2.23.0
                ResponseActionMigration().migrateImport(base)
            }
            68L -> { // 3.2.0
                RequireConfirmationMigration().migrateImport(base)
            }
            71L -> { // 3.4.0
                FileUploadTypeMigration().migrateImport(base)
            }
            78L -> { // 3.15.0
                WorkingDirectoryMigration().migrateImport(base)
            }
        }
    }
}
