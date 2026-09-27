package ch.rmy.android.http_shortcuts.data.migrations

import android.content.ContentValues
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.room.OnConflictStrategy
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteDatabase
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.Application

class Migration14 : AutoMigrationSpec {
    override fun onPostMigrate(db: SupportSQLiteDatabase) {
        db.execSQL("UPDATE shortcut SET exclude_from_file_sharing = true WHERE execution_type = 'scripting'")

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Application.appContext)
        sharedPreferences.getString(KEY_USER_AGENT, null)?.takeUnlessEmpty()
            ?.let { userAgent ->
                val updatedRows = db.update(
                    table = "app_config",
                    conflictAlgorithm = OnConflictStrategy.IGNORE,
                    values = ContentValues().apply {
                        put("user_agent", userAgent)
                    },
                    whereClause = null,
                    whereArgs = null,
                )
                if (updatedRows == 0) {
                    db.insert(
                        table = "app_config",
                        conflictAlgorithm = OnConflictStrategy.IGNORE,
                        values = ContentValues().apply {
                            put("id", 1)
                            put("title", "")
                            put("global_code", "")
                            put("user_agent", userAgent)
                        },
                    )
                }
            }
        sharedPreferences.edit {
            remove(KEY_USER_AGENT)
        }
    }

    companion object {
        private const val KEY_USER_AGENT = "user_agent"
    }
}
