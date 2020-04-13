package ch.rmy.android.http_shortcuts.data.migration.migrations

import com.google.gson.JsonObject
import io.realm.DynamicRealm

class ParameterTypeMigration : BaseMigration {

    override val version: Int = 34

    override fun migrateRealm(realm: DynamicRealm) {
        val schema = realm.schema
        schema.get("Parameter")!!
            .addField("type", String::class.java)
            .addField("fileName", String::class.java)
        realm.where("Parameter").findAll().forEach { category ->
            category.setString("type", "string")
            category.setString("fileName", "string")
        }
        schema.get("Parameter")!!
            .setRequired("type", true)
            .setRequired("fileName", true)
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