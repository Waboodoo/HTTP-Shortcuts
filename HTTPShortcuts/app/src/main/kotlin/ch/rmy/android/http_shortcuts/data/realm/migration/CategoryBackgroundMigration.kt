package ch.rmy.android.http_shortcuts.data.realm.migration

import io.realm.kotlin.migration.AutomaticSchemaMigration

class CategoryBackgroundMigration : RealmMigration {
    override fun migrateRealm(migrationContext: AutomaticSchemaMigration.MigrationContext) {
        migrationContext.enumerate("Category") { oldCategory, newCategory ->
            when (oldCategory.getString("background")) {
                "white" -> newCategory?.set("background", "default")
                "black" -> newCategory?.set("background", "color=#000000")
            }
        }
    }
}
