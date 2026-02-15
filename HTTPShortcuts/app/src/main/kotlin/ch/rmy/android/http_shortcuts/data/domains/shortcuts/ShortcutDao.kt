package ch.rmy.android.http_shortcuts.data.domains.shortcuts

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.sections.SectionId
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import kotlinx.coroutines.flow.Flow

@Dao
interface ShortcutDao {
    @Query("SELECT * FROM shortcut WHERE id != ${Shortcut.TEMPORARY_ID} ORDER BY sorting_order ASC")
    suspend fun getShortcuts(): List<Shortcut>

    @Query("SELECT * FROM shortcut WHERE id != ${Shortcut.TEMPORARY_ID} AND quick_settings_tile_shortcut = 1 ORDER BY name")
    suspend fun getQuickSettingsShortcuts(): List<Shortcut>

    @Query("SELECT * FROM shortcut WHERE id != ${Shortcut.TEMPORARY_ID} ORDER BY sorting_order ASC")
    fun observeShortcuts(): Flow<List<Shortcut>>

    @Query("SELECT * FROM shortcut WHERE category_id = :categoryId AND id != ${Shortcut.TEMPORARY_ID} ORDER BY sorting_order")
    fun observeShortcutsByCategoryId(categoryId: CategoryId): Flow<List<Shortcut>>

    @Query("SELECT * FROM shortcut WHERE id = :shortcutId LIMIT 1")
    fun observeShortcutById(shortcutId: ShortcutId): Flow<List<Shortcut>>

    @Query("SELECT * FROM shortcut WHERE id = :shortcutId LIMIT 1")
    suspend fun getShortcutById(shortcutId: ShortcutId): List<Shortcut>

    @Query("SELECT * FROM shortcut WHERE id = :shortcutNameOrId OR name = :shortcutNameOrId LIMIT 1 COLLATE NOCASE")
    suspend fun getShortcutByNameOrId(shortcutNameOrId: ShortcutNameOrId): List<Shortcut>

    @Query("SELECT * FROM shortcut WHERE id IN (:shortcutIds)")
    suspend fun getShortcutsByIds(shortcutIds: Collection<ShortcutId>): List<Shortcut>

    @Query("SELECT * FROM shortcut WHERE category_id = :categoryId AND id != ${Shortcut.TEMPORARY_ID} ORDER BY sorting_order ASC")
    suspend fun getShortcutsByCategoryId(categoryId: CategoryId): List<Shortcut>

    @Query("SELECT id FROM shortcut WHERE category_id = :categoryId AND id != ${Shortcut.TEMPORARY_ID}")
    suspend fun getShortcutIdsByCategoryId(categoryId: CategoryId): List<ShortcutId>

    @Query("SELECT COUNT(*) FROM shortcut WHERE category_id = :categoryId AND id != ${Shortcut.TEMPORARY_ID}")
    suspend fun getShortcutCountByCategoryId(categoryId: CategoryId): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateShortcut(shortcut: Shortcut)

    @Query("UPDATE shortcut SET category_id = :categoryId, section_id = :sectionId, sorting_order = :sortingOrder WHERE id = :shortcutId")
    suspend fun moveShortcut(shortcutId: ShortcutId, categoryId: CategoryId, sectionId: SectionId?, sortingOrder: Int)

    @Query("DELETE FROM shortcut WHERE id = :shortcutId")
    suspend fun deleteShortcutById(shortcutId: ShortcutId)

    @Query("DELETE FROM shortcut WHERE category_id = :categoryId AND id != ${Shortcut.TEMPORARY_ID}")
    suspend fun deleteShortcutsByCategoryId(categoryId: CategoryId)

    @Query("DELETE FROM shortcut WHERE id != ${Shortcut.TEMPORARY_ID}")
    suspend fun deleteAllShortcuts()

    @Query(
        "UPDATE shortcut SET sorting_order = sorting_order + :diff " +
            "WHERE category_id = :categoryId AND id != ${Shortcut.TEMPORARY_ID} AND sorting_order >= :from AND sorting_order <= :until",
    )
    suspend fun updateSortingOrder(categoryId: CategoryId, from: Int, until: Int, diff: Int)

    @Query("SELECT COUNT(*) FROM shortcut WHERE id != ${Shortcut.TEMPORARY_ID} AND secondary_launcher_shortcut = 1 LIMIT 1")
    suspend fun countSecondaryLauncherShortcuts(): Int
}
