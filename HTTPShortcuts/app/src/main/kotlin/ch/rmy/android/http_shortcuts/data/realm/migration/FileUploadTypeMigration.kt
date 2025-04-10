package ch.rmy.android.http_shortcuts.data.realm.migration

import ch.rmy.android.http_shortcuts.data.enums.FileUploadType
import ch.rmy.android.http_shortcuts.data.models.FileUploadOptions
import ch.rmy.android.http_shortcuts.data.realm.getString
import io.realm.kotlin.migration.AutomaticSchemaMigration

class FileUploadTypeMigration : RealmMigration {
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
                        },
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
                        },
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
                        },
                    )
                }
            }
        }
    }
}
