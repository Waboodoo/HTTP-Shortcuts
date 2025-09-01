package ch.rmy.android.http_shortcuts.data.domains.working_directories

import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.domains.BaseRepository
import ch.rmy.android.http_shortcuts.data.models.WorkingDirectory
import javax.inject.Inject

class WorkingDirectoryRepository
@Inject
constructor(
    database: Database,
) : BaseRepository(database) {
    suspend fun getWorkingDirectories(): List<WorkingDirectory> = query {
        workingDirectoryDao().getWorkingDirectories()
    }
}
