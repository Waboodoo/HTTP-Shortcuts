package ch.rmy.android.http_shortcuts.data.migration.migrations

import ch.rmy.android.http_shortcuts.data.migration.getString
import ch.rmy.android.http_shortcuts.extensions.getArrayOrEmpty
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
        base.getArrayOrEmpty("categories")
            .map { it.asJsonObject }
            .forEach { category ->
                when (category.get("layoutType")?.takeUnless { it.isJsonNull }?.asString) {
                    "grid" -> category.addProperty("layoutType", "dense_grid")
                }
            }
    }
}
