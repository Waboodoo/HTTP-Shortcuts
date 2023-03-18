package ch.rmy.android.http_shortcuts.data.migration.migrations

import ch.rmy.android.http_shortcuts.data.migration.getString
import ch.rmy.android.http_shortcuts.extensions.getArrayOrEmpty
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
        for (category in base.getArrayOrEmpty("categories")) {
            for (shortcut in category.asJsonObject.getArrayOrEmpty("shortcuts")) {
                val shortcutObject = shortcut.asJsonObject
                val responseHandlingObject = shortcutObject.get("responseHandling")?.takeIf { it.isJsonObject }?.asJsonObject ?: continue
                if (responseHandlingObject.get("uiType")?.asString == "window") {
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
