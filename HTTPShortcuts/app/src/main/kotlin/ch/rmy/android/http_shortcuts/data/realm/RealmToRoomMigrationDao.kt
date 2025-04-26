package ch.rmy.android.http_shortcuts.data.realm

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.RequestHeader
import ch.rmy.android.http_shortcuts.data.models.RequestParameter
import ch.rmy.android.http_shortcuts.data.models.Section
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.Variable

@Deprecated("Must only be used for Realm-to-Room migration")
@Dao
interface RealmToRoomMigrationDao {
    @Insert(onConflict = REPLACE)
    suspend fun insertCategory(category: Category)

    @Insert(onConflict = REPLACE)
    suspend fun insertSection(section: Section)

    @Insert(onConflict = REPLACE)
    suspend fun insertShortcut(shortcut: Shortcut)

    @Insert(onConflict = REPLACE)
    suspend fun insertVariable(variable: Variable)

    @Insert(onConflict = REPLACE)
    suspend fun insertRequestHeader(requestHeader: RequestHeader)

    @Insert(onConflict = REPLACE)
    suspend fun insertRequestParameter(requestParameter: RequestParameter)
}
