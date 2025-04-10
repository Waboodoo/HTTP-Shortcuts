package ch.rmy.android.http_shortcuts.import_export.migration

import ch.rmy.android.http_shortcuts.import_export.getObjectArray
import ch.rmy.android.http_shortcuts.import_export.getString
import com.google.gson.JsonObject

class CategoryLayoutMigration : ImportMigration {
    override fun migrateImport(base: JsonObject) {
        base.getObjectArray("categories")
            .forEach { category ->
                when (category.getString("layoutType")) {
                    "grid" -> category.addProperty("layoutType", "dense_grid")
                }
            }
    }
}
