package ch.rmy.android.http_shortcuts.data.migrations

import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteDatabase

class Migration14 : AutoMigrationSpec {
    override fun onPostMigrate(db: SupportSQLiteDatabase) {
        db.execSQL("UPDATE shortcut SET exclude_from_file_sharing = true WHERE execution_type = 'scripting'")
    }
}
