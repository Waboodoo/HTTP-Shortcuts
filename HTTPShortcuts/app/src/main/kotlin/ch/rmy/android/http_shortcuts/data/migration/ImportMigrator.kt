package ch.rmy.android.http_shortcuts.data.migration

import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.data.migration.migrations.CategoryBackgroundMigration
import ch.rmy.android.http_shortcuts.data.migration.migrations.CategoryLayoutMigration
import ch.rmy.android.http_shortcuts.data.migration.migrations.ParameterTypeMigration
import ch.rmy.android.http_shortcuts.data.migration.migrations.RemoveLegacyActionsMigration
import ch.rmy.android.http_shortcuts.data.migration.migrations.ReplaceActionsWithScriptsMigration
import ch.rmy.android.http_shortcuts.data.migration.migrations.ReplaceVariableKeysWithIdsMigration
import ch.rmy.android.http_shortcuts.data.migration.migrations.ResponseActionMigration
import ch.rmy.android.http_shortcuts.data.migration.migrations.ResponseHandlingMigration
import ch.rmy.android.http_shortcuts.extensions.getArrayOrEmpty
import com.google.gson.JsonElement
import com.google.gson.JsonObject

internal object ImportMigrator {

    fun migrate(importData: JsonElement): JsonElement {
        val base = importData.asJsonObject
        val fromVersion = base["version"]?.takeUnless { it.isJsonNull }?.asLong ?: 0L
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
                for (category in base.getArrayOrEmpty("categories")) {
                    category.asJsonObject.addProperty("layoutType", "linear_list")
                }
            }
            6L -> { // 1.16.0
                for (category in base.getArrayOrEmpty("categories")) {
                    for (shortcut in category.getArrayOrEmpty("shortcuts")) {
                        val username = shortcut.asJsonObject["username"].asString
                        val password = shortcut.asJsonObject["password"].asString
                        if (!username.isNullOrEmpty() || !password.isNullOrEmpty()) {
                            shortcut.asJsonObject.addProperty("authentication", "basic")
                        }
                    }
                }
            }
            9L -> { // 1.16.2
                for (category in base.getArrayOrEmpty("categories")) {
                    for (shortcut in category.getArrayOrEmpty("shortcuts")) {
                        for (header in shortcut.getArrayOrEmpty("headers")) {
                            header.asJsonObject.addProperty("id", newUUID())
                        }
                        for (parameter in shortcut.getArrayOrEmpty("parameters")) {
                            parameter.asJsonObject.addProperty("id", newUUID())
                        }
                    }
                }
                for (variable in base.getArrayOrEmpty("variables")) {
                    if (!variable.asJsonObject["options"].isJsonNull) {
                        for (option in variable.getArrayOrEmpty("options")) {
                            option.asJsonObject.addProperty("id", newUUID())
                        }
                    }
                }
            }
            10L -> { // 1.17.0
                for (category in base.getArrayOrEmpty("categories")) {
                    for (shortcut in category.getArrayOrEmpty("shortcuts")) {
                        if (shortcut.asJsonObject["authentication"]?.isJsonNull != false) {
                            shortcut.asJsonObject.addProperty("authentication", "none")
                            shortcut.asJsonObject.addProperty("contentType", "text/plain")
                        }
                    }
                }
            }
            16L -> { // 1.20.0
                for (category in base.getArrayOrEmpty("categories")) {
                    for (shortcut in category.getArrayOrEmpty("shortcuts")) {
                        shortcut.asJsonObject.addProperty("contentType", "text/plain")
                        shortcut.asJsonObject.addProperty(
                            "requestBodyType",
                            if (shortcut.getArrayOrEmpty("parameters").size() == 0) {
                                "custom_text"
                            } else {
                                "x_www_form_urlencode"
                            },
                        )
                    }
                }
            }
            18L -> { // 1.21.0
                for (category in base.getArrayOrEmpty("categories")) {
                    for (shortcut in category.getArrayOrEmpty("shortcuts")) {
                        shortcut.asJsonObject.addProperty("executionType", "app")
                    }
                }
            }
            22L -> { // 1.24.0
                for (category in base.getArrayOrEmpty("categories")) {
                    val oldCategoryId = category.asJsonObject["id"].asLong
                    category.asJsonObject.remove("id")
                    category.asJsonObject.addProperty("id", oldCategoryId.toString())
                    for (shortcut in category.getArrayOrEmpty("shortcuts")) {
                        val oldShortcutId = shortcut.asJsonObject["id"].asLong
                        shortcut.asJsonObject.remove("id")
                        shortcut.asJsonObject.addProperty("id", oldShortcutId.toString())
                    }
                }
                for (variable in base.getArrayOrEmpty("variables")) {
                    val oldVariableId = variable.asJsonObject["id"].asLong
                    variable.asJsonObject.remove("id")
                    variable.asJsonObject.addProperty("id", oldVariableId.toString())
                }
            }
            23L -> { // 1.24.0
                for (category in base.getArrayOrEmpty("categories")) {
                    category.asJsonObject.addProperty("background", "white")
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
                for (category in base.getArrayOrEmpty("categories")) {
                    for (shortcut in category.getArrayOrEmpty("shortcuts")) {
                        val clientCertAlias = shortcut.asJsonObject.get("clientCertAlias")?.takeIf { it.isJsonPrimitive }?.asString
                        if (!clientCertAlias.isNullOrEmpty()) {
                            shortcut.asJsonObject.addProperty("clientCert", "alias:$clientCertAlias")
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
        }
    }
}
