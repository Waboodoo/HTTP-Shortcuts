package ch.rmy.android.http_shortcuts.data.migration.migrations

import com.google.gson.JsonObject
import io.realm.kotlin.migration.AutomaticSchemaMigration

interface BaseMigration {
    fun migrateRealm(migrationContext: AutomaticSchemaMigration.MigrationContext) {
    }

    fun migrateImport(base: JsonObject) {
    }
}
