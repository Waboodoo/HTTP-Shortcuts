package ch.rmy.android.http_shortcuts.data.migration.migrations

import ch.rmy.android.http_shortcuts.data.migration.getObjectArray
import com.google.gson.JsonObject
import io.realm.kotlin.migration.AutomaticSchemaMigration

class ParameterTypeMigration : BaseMigration {

    override fun migrateRealm(migrationContext: AutomaticSchemaMigration.MigrationContext) {
        migrationContext.enumerate("Parameter") { _, newParameter ->
            newParameter?.set("type", "string")
            newParameter?.set("fileName", "")
        }
    }

    override fun migrateImport(base: JsonObject) {
        for (category in base.getObjectArray("categories")) {
            for (shortcut in category.getObjectArray("shortcuts")) {
                for (parameter in shortcut.getObjectArray("parameters")) {
                    parameter.addProperty("type", "string")
                    parameter.addProperty("fileName", "")
                }
            }
        }
    }
}
