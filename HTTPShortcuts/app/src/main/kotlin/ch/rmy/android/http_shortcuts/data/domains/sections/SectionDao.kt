package ch.rmy.android.http_shortcuts.data.domains.sections

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.models.Section
import kotlinx.coroutines.flow.Flow

@Dao
interface SectionDao {
    @Query("SELECT * FROM section WHERE id = :id LIMIT 1")
    suspend fun getSectionById(id: SectionId): List<Section>

    @Query("SELECT * FROM section ORDER BY sorting_order ASC")
    suspend fun getSections(): List<Section>

    @Query("SELECT * FROM section WHERE category_id = :categoryId ORDER BY sorting_order ASC")
    suspend fun getSectionByCategoryId(categoryId: CategoryId): List<Section>

    @Query("SELECT * FROM section ORDER BY sorting_order ASC")
    fun observeSections(): Flow<List<Section>>

    @Query("SELECT * FROM section WHERE category_id = :categoryId ORDER BY sorting_order ASC")
    fun observeSectionsByCategoryId(categoryId: CategoryId): Flow<List<Section>>

    @Query("SELECT COUNT(*) FROM section WHERE category_id = :categoryId")
    suspend fun getSectionCountByCategoryId(categoryId: CategoryId): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSection(section: Section)

    @Query("DELETE FROM section WHERE id = :id")
    suspend fun deleteSection(id: SectionId)

    @Query("DELETE FROM section WHERE category_id = :categoryId")
    suspend fun deleteSectionsByCategoryId(categoryId: CategoryId)

    @Query("DELETE FROM section")
    suspend fun deleteAllSections()

    @Query(
        "UPDATE section SET sorting_order = sorting_order + :diff " +
            "WHERE category_id = :categoryId AND sorting_order >= :from AND sorting_order <= :until",
    )
    suspend fun updateSortingOrder(categoryId: CategoryId, from: Int, until: Int, diff: Int)
}
