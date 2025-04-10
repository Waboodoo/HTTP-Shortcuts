package ch.rmy.android.http_shortcuts.data.realm.migration

import io.realm.kotlin.dynamic.getValue
import io.realm.kotlin.migration.AutomaticSchemaMigration

class RequireConfirmationMigration : RealmMigration {
    override fun migrateRealm(migrationContext: AutomaticSchemaMigration.MigrationContext) {
        migrationContext.enumerate("Shortcut") { oldShortcut, newShortcut ->
            if (oldShortcut.getValue("requireConfirmation")) {
                newShortcut?.set("confirmation", "simple")
            }
        }
    }
}
