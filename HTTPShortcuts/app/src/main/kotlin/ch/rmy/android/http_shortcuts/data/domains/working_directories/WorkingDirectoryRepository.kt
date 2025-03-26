package ch.rmy.android.http_shortcuts.data.domains.working_directories

import android.net.Uri
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.domains.BaseRepository
import ch.rmy.android.http_shortcuts.data.models.WorkingDirectory
import java.time.Instant.now
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class WorkingDirectoryRepository
@Inject
constructor(
    database: Database,
) : BaseRepository(database) {
    fun observeWorkingDirectories(): Flow<List<WorkingDirectory>> = queryFlow {
        workingDirectoryDao().observeWorkingDirectories()
    }

    suspend fun getWorkingDirectories(): List<WorkingDirectory> = query {
        workingDirectoryDao().getWorkingDirectories()
    }

    suspend fun getWorkingDirectoryById(id: WorkingDirectoryId): WorkingDirectory = query {
        workingDirectoryDao().getWorkingDirectoryById(id).first()
    }

    suspend fun getWorkingDirectoryByNameOrId(nameOrId: String): WorkingDirectory = query {
        workingDirectoryDao().getWorkingDirectoryByNameOrId(nameOrId).first()
    }

    suspend fun createWorkingDirectory(name: String, directoryUri: Uri): WorkingDirectory = query {
        val dao = workingDirectoryDao()
        val workingDirectories = dao.getWorkingDirectories()

        var finalName = name
        var counter = 2
        while (workingDirectories.any { it.name == finalName }) {
            finalName = "$name $counter"
            counter++
        }

        val newWorkingDirectory = WorkingDirectory(
            id = newUUID(),
            name = finalName,
            directory = directoryUri,
            accessed = null,
        )
        dao.insertOrUpdateWorkingDirectory(newWorkingDirectory)
        newWorkingDirectory
    }

    suspend fun setDirectoryUri(id: WorkingDirectoryId, directoryUri: Uri) = query {
        workingDirectoryDao()
            .updateWorkingDirectory(id) {
                it.copy(directory = directoryUri)
            }
    }

    suspend fun touchWorkingDirectory(id: WorkingDirectoryId) = query {
        workingDirectoryDao()
            .updateWorkingDirectory(id) {
                it.copy(accessed = now())
            }
    }

    suspend fun renameWorkingDirectory(id: WorkingDirectoryId, newName: String) = query {
        workingDirectoryDao()
            .updateWorkingDirectory(id) {
                it.copy(name = newName)
            }
    }

    suspend fun deleteWorkingDirectory(id: WorkingDirectoryId) = query {
        workingDirectoryDao().deleteWorkingDirectory(id)
    }
}
