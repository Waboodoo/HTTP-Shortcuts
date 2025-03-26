package ch.rmy.android.http_shortcuts.data.realm.migration

import io.realm.kotlin.dynamic.getValueList
import io.realm.kotlin.migration.AutomaticSchemaMigration

class ResponseActionMigration : RealmMigration {
    override fun migrateRealm(migrationContext: AutomaticSchemaMigration.MigrationContext) {
        migrationContext.enumerate("ResponseHandling") { oldResponseHandling, newResponseHandling ->
            if (oldResponseHandling.getString("uiType") == "window") {
                newResponseHandling?.getValueList<String>("actions")?.apply {
                    clear()
                    add("rerun")
                    add("share")
                    add("save")
                }
            }
        }
    }
}
