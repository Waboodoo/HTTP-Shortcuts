package ch.rmy.android.http_shortcuts.data.realm.migration

import io.realm.kotlin.migration.AutomaticSchemaMigration

class CategoryLayoutMigration : RealmMigration {
    override fun migrateRealm(migrationContext: AutomaticSchemaMigration.MigrationContext) {
        migrationContext.enumerate("Category") { oldCategory, newCategory ->
            if (oldCategory.getString("layoutType") == "grid") {
                newCategory?.set("layoutType", "dense_grid")
            }
        }
    }
}
