package ch.rmy.android.http_shortcuts.data.migration.migrations

import ch.rmy.android.http_shortcuts.data.enums.FileUploadType
import ch.rmy.android.http_shortcuts.data.migration.getObjectArray
import ch.rmy.android.http_shortcuts.data.migration.getOrCreateObject
import ch.rmy.android.http_shortcuts.data.migration.getString
import ch.rmy.android.http_shortcuts.data.models.FileUploadOptions
import com.google.gson.JsonObject
import io.realm.kotlin.migration.AutomaticSchemaMigration

class FileUploadTypeMigration : BaseMigration {
    override fun migrateRealm(migrationContext: AutomaticSchemaMigration.MigrationContext) {
        migrationContext.enumerate("Shortcut") { oldShortcut, newShortcut ->
            newShortcut ?: return@enumerate
            if (oldShortcut.getString("requestBodyType") == "image") {
                newShortcut.set("requestBodyType", "file")
                val fileUploadOptions = newShortcut.getObject("fileUploadOptions")
                if (fileUploadOptions != null) {
                    fileUploadOptions.set("fileUploadType", "camera")
                } else {
                    newShortcut.set(
                        "fileUploadOptions",
                        FileUploadOptions().apply {
                            this.type = FileUploadType.CAMERA
                        }
                    )
                }
            }
        }

        migrationContext.enumerate("Parameter") { oldParameter, newParameter ->
            newParameter ?: return@enumerate
            if (oldParameter.getString("type") == "image") {
                newParameter.set("type", "file")
                val fileUploadOptions = newParameter.getObject("fileUploadOptions")
                if (fileUploadOptions != null) {
                    fileUploadOptions.set("fileUploadType", "camera")
                } else {
                    newParameter.set(
                        "fileUploadOptions",
                        FileUploadOptions().apply {
                            this.type = FileUploadType.CAMERA
                        }
                    )
                }
            } else if (oldParameter.getString("type") == "files") {
                newParameter.set("type", "file")
                val fileUploadOptions = newParameter.getObject("fileUploadOptions")
                if (fileUploadOptions != null) {
                    fileUploadOptions.set("fileUploadType", "file_picker_multi")
                } else {
                    newParameter.set(
                        "fileUploadOptions",
                        FileUploadOptions().apply {
                            this.type = FileUploadType.FILE_PICKER_MULTI
                        }
                    )
                }
            }
        }
    }

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
