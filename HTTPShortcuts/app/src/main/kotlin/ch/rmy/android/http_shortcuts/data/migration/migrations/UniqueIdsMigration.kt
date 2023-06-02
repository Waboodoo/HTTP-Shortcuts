package ch.rmy.android.http_shortcuts.data.migration.migrations

import ch.rmy.android.framework.utils.UUIDUtils
import ch.rmy.android.http_shortcuts.data.migration.getString
import io.realm.kotlin.dynamic.DynamicMutableRealm
import io.realm.kotlin.migration.AutomaticSchemaMigration

class UniqueIdsMigration : BaseMigration {

    override fun migrateRealm(migrationContext: AutomaticSchemaMigration.MigrationContext) {
        val newRealm = migrationContext.newRealm
        listOf("Shortcut", "Category", "Variable", "Option", "Header", "Parameter").forEach {
            ensureUniqueIds(newRealm, it)
        }
    }

    private fun ensureUniqueIds(newRealm: DynamicMutableRealm, modelName: String) {
        newRealm.query(modelName).find()
            .let { result ->
                val usedIds = mutableSetOf<String>()
                result.forEach { model ->
                    val id = model.getString("id")!!
                    if (id in usedIds) {
                        val newId = UUIDUtils.newUUID()
                        model.set("id", newId)
                        usedIds.add(newId)
                    } else {
                        usedIds.add(id)
                    }
                }
            }
    }
}
