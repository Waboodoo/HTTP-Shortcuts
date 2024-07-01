package ch.rmy.android.http_shortcuts.activities.workingdirectories.models

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.domains.working_directories.WorkingDirectoryId
import java.time.LocalDateTime

@Stable
data class WorkingDirectoryListItem(
    val id: WorkingDirectoryId,
    val name: String,
    val lastAccessed: LocalDateTime?,
    val unmounted: Boolean,
)
