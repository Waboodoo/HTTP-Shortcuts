package ch.rmy.android.http_shortcuts.data.domains.working_directories

import android.net.Uri
import ch.rmy.android.framework.data.BaseRepository
import ch.rmy.android.framework.data.RealmFactory
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.data.domains.getBase
import ch.rmy.android.http_shortcuts.data.domains.getWorkingDirectory
import ch.rmy.android.http_shortcuts.data.domains.getWorkingDirectoryByNameOrId
import ch.rmy.android.http_shortcuts.data.models.WorkingDirectory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WorkingDirectoryRepository
@Inject
constructor(
    realmFactory: RealmFactory,
) : BaseRepository(realmFactory) {
    fun getObservableWorkingDirectories(): Flow<List<WorkingDirectory>> =
        observeList {
            getBase().findFirst()!!.workingDirectories
        }
            .map { workingDirectories ->
                workingDirectories.sortedBy { it.name.lowercase() }
            }

    suspend fun getWorkingDirectoryById(id: WorkingDirectoryId): WorkingDirectory =
        queryItem {
            getWorkingDirectory(id)
        }

    suspend fun getWorkingDirectoryByNameOrId(nameOrId: String): WorkingDirectory =
        queryItem {
            this.getWorkingDirectoryByNameOrId(nameOrId)
        }

    suspend fun createWorkingDirectory(name: String, directoryUri: Uri): WorkingDirectory {
        val workingDirectory = WorkingDirectory()
        commitTransaction {
            val base = getBase()
                .findFirst()
                ?: return@commitTransaction

            var finalName = name
            var counter = 2
            while (base.workingDirectories.any { it.name == finalName }) {
                finalName = "$name $counter"
                counter++
            }

            workingDirectory.id = newUUID()
            workingDirectory.name = finalName
            workingDirectory.directoryUri = directoryUri
            base.workingDirectories.add(copy(workingDirectory))
        }
        return workingDirectory
    }

    suspend fun setDirectoryUri(id: WorkingDirectoryId, directoryUri: Uri) {
        commitTransaction {
            getWorkingDirectory(id).findFirst()?.directoryUri = directoryUri
        }
    }

    suspend fun touchWorkingDirectory(id: WorkingDirectoryId) {
        commitTransaction {
            getWorkingDirectory(id).findFirst()?.touch()
        }
    }

    suspend fun renameWorkingDirectory(id: WorkingDirectoryId, newName: String) {
        commitTransaction {
            getWorkingDirectory(id).findFirst()?.name = newName
        }
    }

    suspend fun deleteWorkingDirectory(workingDirectoryId: WorkingDirectoryId) {
        commitTransaction {
            getWorkingDirectory(workingDirectoryId).findFirst()?.delete()
        }
    }
}
