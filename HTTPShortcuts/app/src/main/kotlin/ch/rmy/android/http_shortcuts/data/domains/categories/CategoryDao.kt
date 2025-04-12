package ch.rmy.android.http_shortcuts.data.domains.categories

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ch.rmy.android.http_shortcuts.data.models.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM category ORDER BY sorting_order ASC")
    suspend fun getCategories(): List<Category>

    @Query("SELECT id FROM category ORDER BY sorting_order ASC")
    suspend fun getCategoryIds(): List<CategoryId>

    @Query("SELECT * FROM category WHERE id = :categoryId LIMIT 1")
    suspend fun getCategoryById(categoryId: CategoryId): List<Category>

    @Query("SELECT * FROM category WHERE id = :categoryNameOrId OR name = :categoryNameOrId LIMIT 1 COLLATE NOCASE")
    suspend fun getCategoryByNameOrId(categoryNameOrId: String): List<Category>

    @Query("SELECT * FROM category ORDER BY sorting_order ASC")
    fun observeCategories(): Flow<List<Category>>

    @Query("SELECT * FROM category WHERE id = :categoryId LIMIT 1")
    fun observeCategory(categoryId: CategoryId): Flow<Category?>

    @Query("SELECT COUNT(*) FROM category")
    suspend fun getCategoryCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCategory(category: Category)

    @Query("DELETE FROM category WHERE id = :id")
    suspend fun deleteCategory(id: CategoryId)

    @Query("UPDATE category SET sorting_order = sorting_order + :diff WHERE sorting_order >= :from AND sorting_order <= :until")
    suspend fun updateSortingOrder(from: Int, until: Int, diff: Int)

    @Query("DELETE FROM category")
    suspend fun deleteAllCategories()
}
