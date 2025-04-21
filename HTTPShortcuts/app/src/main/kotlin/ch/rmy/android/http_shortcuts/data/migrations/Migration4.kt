package ch.rmy.android.http_shortcuts.data.migrations

import androidx.room.DeleteColumn
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteDatabase

@DeleteColumn(tableName = "shortcut", columnName = "file_upload_source_file")
@DeleteColumn(tableName = "request_parameter", columnName = "file_upload_source_file")
class Migration4 : AutoMigrationSpec {
    override fun onPostMigrate(db: SupportSQLiteDatabase) {
        db.execSQL("UPDATE shortcut SET file_upload_type = 'file_picker' WHERE file_upload_type = 'stored_file'")
        db.execSQL("UPDATE request_parameter SET file_upload_type = 'file_picker' WHERE file_upload_type = 'stored_file'")
    }
}
