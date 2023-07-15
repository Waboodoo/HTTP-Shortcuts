package ch.rmy.android.http_shortcuts.data.migration.migrations

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.realm.kotlin.dynamic.getValue
import io.realm.kotlin.migration.AutomaticSchemaMigration

class RequireConfirmationMigration : BaseMigration {
    override fun migrateRealm(migrationContext: AutomaticSchemaMigration.MigrationContext) {
        migrationContext.enumerate("Shortcut") { oldShortcut, newShortcut ->
            if (oldShortcut.getValue("requireConfirmation")) {
                newShortcut?.set("confirmation", "simple")
            }
        }
    }

    override fun migrateImport(base: JsonObject) {
        for (category in base["categories"].asJsonArray) {
            for (shortcut in category.asJsonObject["shortcuts"]?.asJsonArray ?: JsonArray()) {
                val shortcutObject = shortcut.asJsonObject
                val requireConfirmation = shortcutObject.get("requireConfirmation")?.asBoolean
                if (requireConfirmation == true) {
                    shortcutObject.addProperty("confirmation", "simple")
                }
            }
        }
    }
}
