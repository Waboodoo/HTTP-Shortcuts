package ch.rmy.android.http_shortcuts.import_export.migration

import ch.rmy.android.http_shortcuts.import_export.getObjectArray
import com.google.gson.JsonObject

class RequireConfirmationMigration : ImportMigration {
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
