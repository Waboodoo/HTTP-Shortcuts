package ch.rmy.android.http_shortcuts.data.realm.migration

import io.realm.kotlin.migration.AutomaticSchemaMigration

class ParameterTypeMigration : RealmMigration {
    override fun migrateRealm(migrationContext: AutomaticSchemaMigration.MigrationContext) {
        migrationContext.enumerate("Parameter") { _, newParameter ->
            newParameter?.set("type", "string")
            newParameter?.set("fileName", "")
        }
    }
}
