package ch.rmy.android.http_shortcuts.data.migration.migrations

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
        for (category in base["categories"].asJsonArray) {
            for (shortcut in category.asJsonObject["shortcuts"].asJsonArray) {
                for (parameter in shortcut.asJsonObject["parameters"].asJsonArray) {
                    parameter.asJsonObject.addProperty("type", "string")
                    parameter.asJsonObject.addProperty("fileName", "")
                }
            }
        }
    }
}
