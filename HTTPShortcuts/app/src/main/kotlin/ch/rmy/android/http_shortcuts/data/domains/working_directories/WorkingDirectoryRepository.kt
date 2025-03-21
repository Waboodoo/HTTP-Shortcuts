package ch.rmy.android.http_shortcuts.data.domains.working_directories

import android.net.Uri
import ch.rmy.android.framework.data.BaseRealmRepository
import ch.rmy.android.framework.data.RealmFactory
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.domains.getBase
import ch.rmy.android.http_shortcuts.data.domains.getWorkingDirectory
import ch.rmy.android.http_shortcuts.data.domains.getWorkingDirectoryByNameOrId
import ch.rmy.android.http_shortcuts.data.models.WorkingDirectory
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WorkingDirectoryRepository
@Inject
constructor(
    database: Database,
    realmFactory: RealmFactory,
) : BaseRealmRepository(database, realmFactory) {
    fun observeWorkingDirectories(): Flow<List<WorkingDirectory>> =
        observeList {
            getBase().findFirst()!!.workingDirectories
        }
            .map { workingDirectories ->
                workingDirectories.sortedBy { it.name.lowercase() }
            }

    suspend fun getWorkingDirectories(): List<WorkingDirectory> =
        queryItem {
            getBase()
        }
            .workingDirectories
            .sortedBy { it.name.lowercase() }

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
