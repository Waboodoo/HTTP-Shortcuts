package ch.rmy.android.http_shortcuts.import_export.migration

import ch.rmy.android.http_shortcuts.import_export.getObjectArray
import com.google.gson.JsonObject

class ParameterTypeMigration : ImportMigration {
    override fun migrateImport(base: JsonObject) {
        for (category in base.getObjectArray("categories")) {
            for (shortcut in category.getObjectArray("shortcuts")) {
                for (parameter in shortcut.getObjectArray("parameters")) {
                    parameter.addProperty("type", "string")
                    parameter.addProperty("fileName", "")
                }
            }
        }
    }
}
