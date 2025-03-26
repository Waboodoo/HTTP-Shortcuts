package ch.rmy.android.http_shortcuts.data.domains.request_headers

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.models.RequestHeader
import kotlinx.coroutines.flow.Flow

@Dao
interface RequestHeaderDao {
    @Query("SELECT * FROM request_header WHERE shortcut_id = :shortcutId ORDER BY sorting_order ASC")
    suspend fun getRequestHeadersByShortcutId(shortcutId: ShortcutId): List<RequestHeader>

    @Query("SELECT * FROM request_header WHERE shortcut_id IN (:shortcutIds) ORDER BY sorting_order ASC")
    suspend fun getRequestHeadersByShortcutIds(shortcutIds: List<ShortcutId>): List<RequestHeader>

    @Query("SELECT * FROM request_header WHERE shortcut_id = :shortcutId ORDER BY sorting_order ASC")
    fun observeRequestHeadersByShortcutId(shortcutId: ShortcutId): Flow<List<RequestHeader>>

    @Query("DELETE FROM request_header WHERE shortcut_id = :shortcutId")
    suspend fun deleteRequestHeaderByShortcutId(shortcutId: ShortcutId)

    @Query("DELETE FROM request_header WHERE shortcut_id IN (:shortcutIds)")
    suspend fun deleteRequestHeadersByShortcutIds(shortcutIds: List<ShortcutId>)

    @Query("SELECT * FROM request_header WHERE id = :id LIMIT 1")
    suspend fun getRequestHeaderById(id: RequestHeaderId): List<RequestHeader>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRequestHeader(header: RequestHeader): Long

    @Query("SELECT COUNT(*) FROM request_header WHERE shortcut_id = :shortcutId")
    suspend fun getRequestHeaderCountByShortcutId(shortcutId: ShortcutId): Int

    @Query("DELETE FROM request_header WHERE id = :id")
    suspend fun deleteRequestHeader(id: RequestHeaderId)

    @Query("DELETE FROM request_header")
    suspend fun deleteAllRequestHeaders()

    @Query(
        "UPDATE request_header SET sorting_order = sorting_order + :diff " +
            "WHERE shortcut_id = :shortcutId AND sorting_order >= :from AND sorting_order <= :until",
    )
    suspend fun updateSortingOrder(shortcutId: ShortcutId, from: Int, until: Int, diff: Int)
}
