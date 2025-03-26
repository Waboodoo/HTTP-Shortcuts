package ch.rmy.android.http_shortcuts.data.realm.migration

import ch.rmy.android.framework.utils.UUIDUtils
import io.realm.kotlin.dynamic.DynamicMutableRealm
import io.realm.kotlin.dynamic.DynamicMutableRealmObject
import io.realm.kotlin.migration.AutomaticSchemaMigration

class UniqueIdsMigration : RealmMigration {
    override fun migrateRealm(migrationContext: AutomaticSchemaMigration.MigrationContext) {
        val newRealm = migrationContext.newRealm
        listOf("Shortcut", "Category", "Variable", "Option", "Header", "Parameter").forEach {
            ensureUniqueIds(newRealm, it)
        }
        ensureUniqueVariableKeys(newRealm)
        ensureUniqueVariableReferences(newRealm)
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

    private fun ensureUniqueVariableKeys(newRealm: DynamicMutableRealm) {
        newRealm.query("Variable").find()
            .let { result ->
                val usedKeys = mutableSetOf<String>()
                result.forEach { model ->
                    val key = model.getString("key")!!
                    if (model.getString("id") == "0") {
                        // ignore the temporary variable
                    } else if (key in usedKeys) {
                        val newKey = key + "_2"
                        model.set("key", newKey)
                        usedKeys.add(newKey)
                    } else {
                        usedKeys.add(key)
                    }
                }
            }
    }

    private fun ensureUniqueVariableReferences(newRealm: DynamicMutableRealm) {
        val base = newRealm.query("Base").find().firstOrNull() ?: return
        val iterator = base.getValueList("variables", DynamicMutableRealmObject::class)
            .iterator()
        val usedIds = mutableSetOf<String>()
        while (iterator.hasNext()) {
            val variable = iterator.next()
            val id = variable.getString("id")!!
            if (id in usedIds) {
                iterator.remove()
            } else {
                usedIds.add(id)
            }
        }
    }
}
