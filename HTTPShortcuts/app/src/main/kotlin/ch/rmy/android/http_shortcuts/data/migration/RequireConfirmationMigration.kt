package ch.rmy.android.http_shortcuts.data.migration

import ch.rmy.android.http_shortcuts.import_export.getObjectArray
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
        for (category in base.getObjectArray("categories")) {
            for (shortcut in category.getObjectArray("shortcuts")) {
                val requireConfirmation = shortcut.get("requireConfirmation")?.asBoolean
                if (requireConfirmation == true) {
                    shortcut.addProperty("confirmation", "simple")
                }
            }
        }
    }
}
