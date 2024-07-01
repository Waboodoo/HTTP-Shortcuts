package ch.rmy.android.http_shortcuts.data.migration.migrations

import androidx.core.net.toUri
import ch.rmy.android.framework.extensions.tryOrLog
import ch.rmy.android.framework.utils.UUIDUtils
import ch.rmy.android.http_shortcuts.data.migration.getObject
import ch.rmy.android.http_shortcuts.data.migration.getObjectArray
import ch.rmy.android.http_shortcuts.data.migration.getString
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.realm.kotlin.dynamic.DynamicMutableRealmObject
import io.realm.kotlin.dynamic.getNullableValue
import io.realm.kotlin.migration.AutomaticSchemaMigration

class WorkingDirectoryMigration : BaseMigration {
    override fun migrateRealm(migrationContext: AutomaticSchemaMigration.MigrationContext) {
        var counter = 1
        val base = migrationContext.newRealm.query("Base").first().find()!!
        val workingDirectories = base.getObjectList("workingDirectories")
        migrationContext.enumerate("ResponseHandling") { oldResponseHandling, newResponseHandling ->
            val storeDirectoryUri = oldResponseHandling.getNullableValue<String>("storeDirectory") ?: return@enumerate
            val id = UUIDUtils.newUUID()

            val workingDirectory = migrationContext.newRealm.copyToRealm(
                DynamicMutableRealmObject.create(
                    type = "WorkingDirectory",
                    mapOf(
                        "id" to id,
                        "name" to getDirectoryName(storeDirectoryUri, counter),
                        "directory" to storeDirectoryUri,
                        "accessed" to null,
                    )
                )
            )
            workingDirectories.add(workingDirectory)

            newResponseHandling?.set("storeDirectoryId", id)
            counter++
        }
    }

    override fun migrateImport(base: JsonObject) {
        var counter = 1
        val workingDirectories = JsonArray()
        for (category in base.getObjectArray("categories")) {
            for (shortcut in category.getObjectArray("shortcuts")) {
                val responseHandlingObject = shortcut.getObject("responseHandling") ?: continue
                val storeDirectoryUri = responseHandlingObject.getString("storeDirectory") ?: continue
                val id = UUIDUtils.newUUID()
                workingDirectories.add(
                    JsonObject()
                        .apply {
                            addProperty("id", id)
                            addProperty("name", getDirectoryName(storeDirectoryUri, counter))
                            addProperty("directory", storeDirectoryUri)
                        }
                )
                responseHandlingObject.addProperty("storeDirectoryId", id)
                counter++
            }
        }
        base.add("workingDirectories", workingDirectories)
    }

    private fun getDirectoryName(uri: String, counter: Int): String =
        tryOrLog {
            uri.toUri().lastPathSegment?.takeLastWhile { it != '/' && it != ':' }
        }
            ?: "dir$counter"
}
