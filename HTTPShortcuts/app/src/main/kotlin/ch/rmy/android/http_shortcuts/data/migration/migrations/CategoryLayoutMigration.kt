package ch.rmy.android.http_shortcuts.data.migration.migrations

import ch.rmy.android.http_shortcuts.extensions.getArrayOrEmpty
import com.google.gson.JsonObject
import io.realm.DynamicRealm

object CategoryLayoutMigration : BaseMigration {

    override fun migrateImport(base: JsonObject) {
        base.getArrayOrEmpty("categories")
            .map { it.asJsonObject }
            .forEach { category ->
                when (category.get("layoutType")?.takeUnless { it.isJsonNull }?.asString) {
                    "grid" -> category.addProperty("layoutType", "dense_grid")
                }
            }
    }

    override fun migrateRealm(realm: DynamicRealm) {
        realm.where("Category")
            .findAll()
            .forEach { category ->
                when (category.getString("layoutType")) {
                    "grid" -> category.setString("layoutType", "dense_grid")
                }
            }
    }

    override val version: Int
        get() = 51
}
