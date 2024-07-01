package ch.rmy.android.http_shortcuts.activities.workingdirectories

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.activities.workingdirectories.models.WorkingDirectoryListItem

@Stable
data class WorkingDirectoriesViewState(
    val dialogState: WorkingDirectoriesDialogState? = null,
    val workingDirectories: List<WorkingDirectoryListItem>,
)
