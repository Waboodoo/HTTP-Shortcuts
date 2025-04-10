package ch.rmy.android.http_shortcuts.import_export.migration

import ch.rmy.android.http_shortcuts.import_export.getObjectArray
import ch.rmy.android.http_shortcuts.import_export.getOrCreateObject
import ch.rmy.android.http_shortcuts.import_export.getString
import com.google.gson.JsonObject

class FileUploadTypeMigration : ImportMigration {
    override fun migrateImport(base: JsonObject) {
        for (category in base.getObjectArray("categories")) {
            for (shortcut in category.getObjectArray("shortcuts")) {
                if (shortcut.getString("requestBodyType") == "image") {
                    shortcut.addProperty("requestBodyType", "file")
                    val fileUploadOptions = shortcut.getOrCreateObject("fileUploadOptions")
                    fileUploadOptions.addProperty("fileUploadType", "camera")
                }

                for (parameter in shortcut.getObjectArray("parameters")) {
                    when (parameter.getString("type")) {
                        "image" -> {
                            parameter.addProperty("type", "file")
                            val fileUploadOptions = parameter.getOrCreateObject("fileUploadOptions")
                            fileUploadOptions.addProperty("fileUploadType", "camera")
                        }
                        "files" -> {
                            parameter.addProperty("type", "file")
                            val fileUploadOptions = parameter.getOrCreateObject("fileUploadOptions")
                            fileUploadOptions.addProperty("fileUploadType", "file_picker_multi")
                        }
                    }
                }
            }
        }
    }
}
