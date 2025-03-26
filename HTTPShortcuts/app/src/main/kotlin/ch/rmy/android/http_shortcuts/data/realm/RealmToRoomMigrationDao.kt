package ch.rmy.android.http_shortcuts.data.realm

import androidx.room.Dao
import androidx.room.Insert
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.RequestHeader
import ch.rmy.android.http_shortcuts.data.models.RequestParameter
import ch.rmy.android.http_shortcuts.data.models.Section
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.Variable

@Deprecated("Must only be used for Realm-to-Room migration")
@Dao
interface RealmToRoomMigrationDao {
    @Insert
    suspend fun insertCategory(category: Category)

    @Insert
    suspend fun insertSection(section: Section)

    @Insert
    suspend fun insertShortcut(shortcut: Shortcut)

    @Insert
    suspend fun insertVariable(variable: Variable)

    @Insert
    suspend fun insertRequestHeader(requestHeader: RequestHeader)

    @Insert
    suspend fun insertRequestParameter(requestParameter: RequestParameter)
}
