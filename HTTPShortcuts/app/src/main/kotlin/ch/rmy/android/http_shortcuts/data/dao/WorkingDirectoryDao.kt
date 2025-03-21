package ch.rmy.android.http_shortcuts.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import ch.rmy.android.http_shortcuts.data.domains.working_directories.WorkingDirectoryId
import ch.rmy.android.http_shortcuts.data.models.WorkingDirectory
import kotlin.collections.forEach
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkingDirectoryDao {
    @Query("SELECT * FROM working_directory ORDER BY name ASC")
    suspend fun get(): List<WorkingDirectory>

    @Query("SELECT * FROM working_directory ORDER BY name ASC")
    fun observe(): Flow<List<WorkingDirectory>>

    @Query("SELECT * FROM working_directory WHERE id = :id")
    suspend fun getById(id: WorkingDirectoryId): List<WorkingDirectory>

    @Query("SELECT * FROM working_directory WHERE id = :nameOrId OR name = :nameOrId")
    suspend fun getByNameOrId(nameOrId: String): List<WorkingDirectory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(workingDirectory: WorkingDirectory)

    @Transaction
    suspend fun insert(workingDirectories: List<WorkingDirectory>) {
        workingDirectories.forEach { workingDirectory ->
            insert(workingDirectory)
        }
    }

    @Transaction
    suspend fun update(id: WorkingDirectoryId, transformation: (WorkingDirectory) -> WorkingDirectory) {
        getById(id)
            .firstOrNull()
            ?.let(transformation)
            ?.let { insert(it) }
    }

    @Transaction
    suspend fun replace(workingDirectories: List<WorkingDirectory>) {
        deleteAll()
        insert(workingDirectories)
    }

    @Query("DELETE FROM working_directory WHERE id = :id")
    suspend fun delete(id: WorkingDirectoryId)

    @Query("DELETE FROM working_directory")
    suspend fun deleteAll()
}
