package ch.rmy.android.http_shortcuts.data.domains.working_directories

import android.net.Uri
import ch.rmy.android.framework.data.BaseRepository
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.models.WorkingDirectory
import ch.rmy.android.http_shortcuts.import_export.Importer
import java.time.Instant.now
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class WorkingDirectoryRepository
@Inject
constructor(
    database: Database,
) : BaseRepository(database) {
    fun observeWorkingDirectories(): Flow<List<WorkingDirectory>> =
        flow(Database::workingDirectoryDao) {
            observe()
                .distinctUntilChanged()
        }

    suspend fun getWorkingDirectories(): List<WorkingDirectory> =
        get(Database::workingDirectoryDao).get()

    suspend fun getWorkingDirectoryById(id: WorkingDirectoryId): WorkingDirectory =
        get(Database::workingDirectoryDao).getById(id).first()

    suspend fun getWorkingDirectoryByNameOrId(nameOrId: String): WorkingDirectory =
        get(Database::workingDirectoryDao).getByNameOrId(nameOrId).first()

    suspend fun createWorkingDirectory(name: String, directoryUri: Uri): WorkingDirectory {
        val dao = get(Database::workingDirectoryDao)
        val workingDirectories = dao.get()

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
        )
        dao.insert(newWorkingDirectory)
        return newWorkingDirectory
    }

    suspend fun setDirectoryUri(id: WorkingDirectoryId, directoryUri: Uri) {
        get(Database::workingDirectoryDao)
            .update(id) {
                it.copy(directory = directoryUri)
            }
    }

    suspend fun touchWorkingDirectory(id: WorkingDirectoryId) {
        get(Database::workingDirectoryDao)
            .update(id) {
                it.copy(accessed = now())
            }
    }

    suspend fun renameWorkingDirectory(id: WorkingDirectoryId, newName: String) {
        get(Database::workingDirectoryDao)
            .update(id) {
                it.copy(name = newName)
            }
    }

    suspend fun deleteWorkingDirectory(id: WorkingDirectoryId) {
        get(Database::workingDirectoryDao).delete(id)
    }

    suspend fun import(workingDirectories: List<WorkingDirectory>, mode: Importer.ImportMode) {
        with(get(Database::workingDirectoryDao)) {
            when (mode) {
                Importer.ImportMode.MERGE -> insert(workingDirectories)
                Importer.ImportMode.REPLACE -> replace(workingDirectories)
            }
        }
    }
}
