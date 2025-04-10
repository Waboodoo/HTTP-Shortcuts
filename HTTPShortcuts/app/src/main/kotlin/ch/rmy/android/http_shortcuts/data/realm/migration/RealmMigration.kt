package ch.rmy.android.http_shortcuts.data.realm.migration

import io.realm.kotlin.migration.AutomaticSchemaMigration

interface RealmMigration {
    fun migrateRealm(migrationContext: AutomaticSchemaMigration.MigrationContext)
}
