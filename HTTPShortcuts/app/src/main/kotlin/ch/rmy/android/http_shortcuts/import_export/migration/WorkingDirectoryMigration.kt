package ch.rmy.android.http_shortcuts.import_export.migration

import androidx.core.net.toUri
import ch.rmy.android.framework.extensions.tryOrLog
import ch.rmy.android.framework.utils.UUIDUtils
import ch.rmy.android.http_shortcuts.import_export.getObject
import ch.rmy.android.http_shortcuts.import_export.getObjectArray
import ch.rmy.android.http_shortcuts.import_export.getString
import com.google.gson.JsonArray
import com.google.gson.JsonObject

class WorkingDirectoryMigration : ImportMigration {
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
                        },
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
