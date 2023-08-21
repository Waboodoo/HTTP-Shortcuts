package ch.rmy.android.http_shortcuts.data.migration.migrations

import ch.rmy.android.http_shortcuts.data.migration.getObjectArray
import ch.rmy.android.http_shortcuts.data.migration.getString
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
