package ch.rmy.android.http_shortcuts.data.migration

import ch.rmy.android.http_shortcuts.data.realm.getString
import ch.rmy.android.http_shortcuts.import_export.getObjectArray
import ch.rmy.android.http_shortcuts.import_export.getString
import com.google.gson.JsonObject
import io.realm.kotlin.migration.AutomaticSchemaMigration

class CategoryLayoutMigration : BaseMigration {

    override fun migrateRealm(migrationContext: AutomaticSchemaMigration.MigrationContext) {
        migrationContext.enumerate("Category") { oldCategory, newCategory ->
            if (oldCategory.getString("layoutType") == "grid") {
                newCategory?.set("layoutType", "dense_grid")
            }
        }
    }

    override fun migrateImport(base: JsonObject) {
        base.getObjectArray("categories")
            .forEach { category ->
                when (category.getString("layoutType")) {
                    "grid" -> category.addProperty("layoutType", "dense_grid")
                }
            }
    }
}
