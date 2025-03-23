package ch.rmy.android.http_shortcuts.data.migration

import ch.rmy.android.http_shortcuts.data.realm.getString
import ch.rmy.android.http_shortcuts.import_export.getObjectArray
import ch.rmy.android.http_shortcuts.import_export.getString
import com.google.gson.JsonObject
import io.realm.kotlin.migration.AutomaticSchemaMigration

class CategoryBackgroundMigration : BaseMigration {

    override fun migrateRealm(migrationContext: AutomaticSchemaMigration.MigrationContext) {
        migrationContext.enumerate("Category") { oldCategory, newCategory ->
            when (oldCategory.getString("background")) {
                "white" -> newCategory?.set("background", "default")
                "black" -> newCategory?.set("background", "color=#000000")
            }
        }
    }

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
