package ch.rmy.android.http_shortcuts.import_export.migration

import ch.rmy.android.http_shortcuts.import_export.getObjectArray
import ch.rmy.android.http_shortcuts.import_export.getString
import com.google.gson.JsonObject

class CategoryBackgroundMigration : ImportMigration {
    override fun migrateImport(base: JsonObject) {
        base.getObjectArray("categories")
            .forEach { category ->
                when (category.getString("background")) {
                    "white" -> category.addProperty("background", "default")
                    "black" -> category.addProperty("background", "color=#000000")
                }
            }
    }
}
