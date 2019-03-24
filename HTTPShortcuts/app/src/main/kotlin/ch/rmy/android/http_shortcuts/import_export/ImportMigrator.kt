package ch.rmy.android.http_shortcuts.import_export


import ch.rmy.android.http_shortcuts.realm.DatabaseMigration
import ch.rmy.android.http_shortcuts.utils.UUIDUtils.newUUID
import com.google.gson.JsonElement
import com.google.gson.JsonObject

internal object ImportMigrator {

    fun migrate(importData: JsonElement): JsonElement {
        var base = importData.asJsonObject
        val fromVersion = base["version"].asInt
        for (version in fromVersion + 1..DatabaseMigration.VERSION) {
            base = migrate(base, version)
            base.addProperty("version", version)
        }
        return base
    }

    private fun migrate(base: JsonObject, newVersion: Long): JsonObject {
        when (newVersion) {
            5L -> { // 1.16.0
                for (category in base["categories"].asJsonArray) {
                    category.asJsonObject.addProperty("layoutType", "linear_list")
                }
            }
            6L -> { // 1.16.0
                for (category in base["categories"].asJsonArray) {
                    for (shortcut in category.asJsonObject["shortcuts"].asJsonArray) {
                        val username = shortcut.asJsonObject["username"].asString
                        val password = shortcut.asJsonObject["password"].asString
                        if (!username.isNullOrEmpty() || !password.isNullOrEmpty()) {
                            shortcut.asJsonObject.addProperty("authentication", "basic")
                        }
                    }
                }
            }
            9L -> { // 1.16.2
                for (category in base["categories"].asJsonArray) {
                    for (shortcut in category.asJsonObject["shortcuts"].asJsonArray) {
                        for (header in shortcut.asJsonObject["headers"].asJsonArray) {
                            header.asJsonObject.addProperty("id", newUUID())
                        }
                        for (parameter in shortcut.asJsonObject["parameters"].asJsonArray) {
                            parameter.asJsonObject.addProperty("id", newUUID())
                        }
                    }
                }
                for (variable in base["variables"].asJsonArray) {
                    if (!variable.asJsonObject["options"].isJsonNull) {
                        for (option in variable.asJsonObject["options"].asJsonArray) {
                            option.asJsonObject.addProperty("id", newUUID())
                        }
                    }
                }
            }
            10L -> { // 1.17.0
                for (category in base["categories"].asJsonArray) {
                    for (shortcut in category.asJsonObject["shortcuts"].asJsonArray) {
                        if (shortcut.asJsonObject["authentication"].isJsonNull) {
                            shortcut.asJsonObject.addProperty("authentication", "none")
                            shortcut.asJsonObject.addProperty("contentType", "text/plain")
                        }
                    }
                }
            }
            16L -> { // 1.20.0
                for (category in base["categories"].asJsonArray) {
                    for (shortcut in category.asJsonObject["shortcuts"].asJsonArray) {
                        shortcut.asJsonObject.addProperty("contentType", "text/plain")
                        shortcut.asJsonObject.addProperty("requestBodyType", if (shortcut.asJsonObject["parameters"].asJsonArray.size() == 0) {
                            "custom_text"
                        } else {
                            "x_www_form_urlencode"
                        })
                    }
                }
            }
            18L -> { // 1.21.0
                for (category in base["categories"].asJsonArray) {
                    for (shortcut in category.asJsonObject["shortcuts"].asJsonArray) {
                        shortcut.asJsonObject.addProperty("executionType", "app")
                    }
                }
            }
            22L -> { // 1.24.0
                for (category in base["categories"].asJsonArray) {
                    val oldCategoryId = category.asJsonObject["id"].asLong
                    category.asJsonObject.remove("id")
                    category.asJsonObject.addProperty("id", oldCategoryId.toString())
                    for (shortcut in category.asJsonObject["shortcuts"].asJsonArray) {
                        val oldShortcutId = shortcut.asJsonObject["id"].asLong
                        shortcut.asJsonObject.remove("id")
                        shortcut.asJsonObject.addProperty("id", oldShortcutId.toString())
                    }
                }
                for (variable in base["variables"].asJsonArray) {
                    val oldVariableId = variable.asJsonObject["id"].asLong
                    variable.asJsonObject.remove("id")
                    variable.asJsonObject.addProperty("id", oldVariableId.toString())
                }
            }
            23L -> { // 1.24.0
                for (category in base["categories"].asJsonArray) {
                    category.asJsonObject.addProperty("background", "white")
                }
            }
        }
        return base
    }

}
