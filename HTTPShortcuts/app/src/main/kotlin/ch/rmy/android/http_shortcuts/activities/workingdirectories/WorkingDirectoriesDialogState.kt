package ch.rmy.android.http_shortcuts.activities.workingdirectories

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.domains.working_directories.WorkingDirectoryId

@Stable
sealed class WorkingDirectoriesDialogState {
    @Stable
    data class ContextMenu(
        val title: String,
        val workingDirectoryId: WorkingDirectoryId,
    ) : WorkingDirectoriesDialogState()

    @Stable
    data class Rename(
        val workingDirectoryId: WorkingDirectoryId,
        val oldName: String,
        val existingNames: Set<String>,
    ) : WorkingDirectoriesDialogState()

    @Stable
    data class Delete(
        val title: String,
        val workingDirectoryId: WorkingDirectoryId,
    ) : WorkingDirectoriesDialogState()
}
