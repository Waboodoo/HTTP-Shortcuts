package ch.rmy.android.http_shortcuts.data.migration.migrations

import ch.rmy.android.http_shortcuts.extensions.getArrayOrEmpty
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.realm.DynamicRealm
import io.realm.RealmList

object ResponseActionMigration : BaseMigration {
    override val version: Int = 53

    override fun migrateRealm(realm: DynamicRealm) {
        realm.schema.get("ResponseHandling")!!
            .addRealmListField("actions", String::class.java)
        realm.where("Shortcut")
            .findAll()
            .forEach { shortcut ->
                shortcut.getObject("responseHandling")
                    ?.let { responseHandling ->
                        if (responseHandling.getString("uiType") == "window") {
                            responseHandling.setList("actions", RealmList("rerun", "share", "save"))
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
