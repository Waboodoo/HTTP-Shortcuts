package ch.rmy.android.http_shortcuts.data.domains.request_parameters

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.models.RequestParameter
import kotlinx.coroutines.flow.Flow

@Dao
interface RequestParameterDao {
    @Query("SELECT * FROM request_parameter WHERE shortcut_id = :shortcutId ORDER BY sorting_order ASC")
    suspend fun getRequestParametersByShortcutId(shortcutId: ShortcutId): List<RequestParameter>

    @Query("SELECT * FROM request_parameter WHERE shortcut_id IN (:shortcutIds) ORDER BY sorting_order ASC")
    suspend fun getRequestParametersByShortcutIds(shortcutIds: List<ShortcutId>): List<RequestParameter>

    @Query("SELECT * FROM request_parameter WHERE shortcut_id = :shortcutId ORDER BY sorting_order ASC")
    fun observeRequestParametersByShortcutId(shortcutId: ShortcutId): Flow<List<RequestParameter>>

    @Query("DELETE FROM request_parameter WHERE shortcut_id = :shortcutId")
    suspend fun deleteRequestParametersByShortcutId(shortcutId: ShortcutId)

    @Query("DELETE FROM request_parameter WHERE shortcut_id IN (:shortcutIds)")
    suspend fun deleteRequestParametersByShortcutIds(shortcutIds: List<ShortcutId>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRequestParameter(parameter: RequestParameter): Long

    @Query("SELECT * FROM request_parameter WHERE id = :id LIMIT 1")
    suspend fun getRequestParameterById(id: RequestParameterId): List<RequestParameter>

    @Query("SELECT COUNT(*) FROM request_parameter WHERE shortcut_id = :shortcutId")
    suspend fun getRequestParameterCountByShortcutId(shortcutId: ShortcutId): Int

    @Query("DELETE FROM request_parameter WHERE id = :id")
    suspend fun deleteRequestParameter(id: RequestParameterId)

    @Query("DELETE FROM request_parameter")
    suspend fun deleteAllRequestParameters()

    @Query(
        "UPDATE request_parameter SET sorting_order = sorting_order + :diff " +
            "WHERE shortcut_id = :shortcutId AND sorting_order >= :from AND sorting_order <= :until",
    )
    suspend fun updateSortingOrder(shortcutId: ShortcutId, from: Int, until: Int, diff: Int)
}
