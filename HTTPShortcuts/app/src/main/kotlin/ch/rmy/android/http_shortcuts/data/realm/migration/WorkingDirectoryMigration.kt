package ch.rmy.android.http_shortcuts.data.realm.migration

import androidx.core.net.toUri
import ch.rmy.android.framework.extensions.tryOrLog
import ch.rmy.android.framework.utils.UUIDUtils
import io.realm.kotlin.dynamic.DynamicMutableRealmObject
import io.realm.kotlin.dynamic.getNullableValue
import io.realm.kotlin.migration.AutomaticSchemaMigration

class WorkingDirectoryMigration : RealmMigration {
    override fun migrateRealm(migrationContext: AutomaticSchemaMigration.MigrationContext) {
        var counter = 1
        migrationContext.enumerate("ResponseHandling") { oldResponseHandling, newResponseHandling ->
            val storeDirectoryUri = oldResponseHandling.getNullableValue<String>("storeDirectory") ?: return@enumerate
            val id = UUIDUtils.newUUID()

            migrationContext.newRealm.copyToRealm(
                DynamicMutableRealmObject.create(
                    type = "WorkingDirectory",
                    mapOf(
                        "id" to id,
                        "name" to getDirectoryName(storeDirectoryUri, counter),
                        "directory" to storeDirectoryUri,
                        "accessed" to null,
                    ),
                ),
            )

            newResponseHandling?.set("storeDirectoryId", id)
            counter++
        }
    }

    private fun getDirectoryName(uri: String, counter: Int): String =
        tryOrLog {
            uri.toUri().lastPathSegment?.takeLastWhile { it != '/' && it != ':' }
        }
            ?: "dir$counter"
}
