package ch.rmy.android.http_shortcuts.data.migration.migrations

import ch.rmy.android.http_shortcuts.data.migration.getObject
import ch.rmy.android.http_shortcuts.data.migration.getObjectArray
import ch.rmy.android.http_shortcuts.data.migration.getString
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.realm.kotlin.dynamic.getValueList
import io.realm.kotlin.migration.AutomaticSchemaMigration

class ResponseActionMigration : BaseMigration {

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

    override fun migrateImport(base: JsonObject) {
        for (category in base.getObjectArray("categories")) {
            for (shortcut in category.getObjectArray("shortcuts")) {
                val responseHandlingObject = shortcut.getObject("responseHandling") ?: continue
                if (responseHandlingObject.getString("uiType") == "window") {
                    val array = JsonArray().apply {
                        add("rerun")
                        add("share")
                        add("save")
                    }
                    responseHandlingObject.add("actions", array)
                } else {
                    responseHandlingObject.add("actions", JsonArray())
                }
            }
        }
    }
}
