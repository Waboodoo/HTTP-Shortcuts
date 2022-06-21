package ch.rmy.android.http_shortcuts.data.migration.migrations

import ch.rmy.android.http_shortcuts.extensions.getArrayOrEmpty
import com.google.gson.JsonObject
import io.realm.DynamicRealm

object CategoryBackgroundMigration : BaseMigration {

    override fun migrateImport(base: JsonObject) {
        base.getArrayOrEmpty("categories")
            .map { it.asJsonObject }
            .forEach { category ->
                when (category.get("background")?.takeUnless { it.isJsonNull }?.asString) {
                    "white" -> category.addProperty("background", "default")
                    "black" -> category.addProperty("background", "color=#000000")
                }
            }
    }

    override fun migrateRealm(realm: DynamicRealm) {
        realm.where("Category")
            .findAll()
            .forEach { category ->
                when (category.getString("background")) {
                    "white" -> category.setString("background", "default")
                    "black" -> category.setString("background", "color=#000000")
                }
            }
    }

    override val version: Int
        get() = 50
}
