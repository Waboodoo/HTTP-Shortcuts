package ch.rmy.android.http_shortcuts.data.domains.working_directories

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import ch.rmy.android.http_shortcuts.data.models.WorkingDirectory
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkingDirectoryDao {
    @Query("SELECT * FROM working_directory ORDER BY name ASC")
    suspend fun getWorkingDirectories(): List<WorkingDirectory>

    @Query("SELECT * FROM working_directory ORDER BY name ASC")
    fun observeWorkingDirectories(): Flow<List<WorkingDirectory>>

    @Query("SELECT * FROM working_directory WHERE id = :id")
    suspend fun getWorkingDirectoryById(id: WorkingDirectoryId): List<WorkingDirectory>

    @Query("SELECT * FROM working_directory WHERE id = :nameOrId OR name = :nameOrId COLLATE NOCASE")
    suspend fun getWorkingDirectoryByNameOrId(nameOrId: String): List<WorkingDirectory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateWorkingDirectory(workingDirectory: WorkingDirectory)

    @Transaction
    suspend fun updateWorkingDirectory(id: WorkingDirectoryId, transformation: (WorkingDirectory) -> WorkingDirectory) {
        getWorkingDirectoryById(id)
            .firstOrNull()
            ?.let(transformation)
            ?.let { insertOrUpdateWorkingDirectory(it) }
    }

    @Query("DELETE FROM working_directory WHERE id = :id")
    suspend fun deleteWorkingDirectory(id: WorkingDirectoryId)

    @Query("DELETE FROM working_directory")
    suspend fun deleteAllWorkingDirectories()
}
