package ch.rmy.android.http_shortcuts.import_export.migration

import ch.rmy.android.http_shortcuts.import_export.getObject
import ch.rmy.android.http_shortcuts.import_export.getObjectArray
import ch.rmy.android.http_shortcuts.import_export.getString
import com.google.gson.JsonArray
import com.google.gson.JsonObject

class ResponseActionMigration : ImportMigration {
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
