package ch.rmy.android.http_shortcuts.activities.workingdirectories

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.workingdirectories.models.WorkingDirectoryListItem
import ch.rmy.android.http_shortcuts.components.EmptyState
import ch.rmy.android.http_shortcuts.data.domains.working_directories.WorkingDirectoryId
import ch.rmy.android.http_shortcuts.extensions.formatMediumDateTime

@Composable
fun WorkingDirectoriesContent(
    workingDirectories: List<WorkingDirectoryListItem>,
    onWorkingDirectoryClicked: (WorkingDirectoryId) -> Unit,
) {
    if (workingDirectories.isEmpty()) {
        EmptyState(
            title = stringResource(R.string.empty_state_working_directories),
            description = stringResource(R.string.empty_state_working_directories_instructions),
        )
        return
    }

    LazyColumn(
        state = rememberLazyListState(),
        modifier = Modifier
            .fillMaxSize(),
    ) {
        items(
            items = workingDirectories,
            contentType = { "workingDirectory" },
            key = { it.id },
        ) { item ->
            WorkingDirectoryItem(
                modifier = Modifier.clickable(
                    onClick = {
                        onWorkingDirectoryClicked(item.id)
                    },
                ),
                workingDirectory = item,
            )
        }
    }
}

@Composable
private fun WorkingDirectoryItem(
    modifier: Modifier,
    workingDirectory: WorkingDirectoryListItem,
) {
    Column(modifier) {
        ListItem(
            modifier = Modifier.fillMaxWidth(),
            headlineContent = {
                Text(workingDirectory.name)
            },
            supportingContent = {
                Text(
                    text = when {
                        workingDirectory.unmounted -> {
                            stringResource(R.string.working_directory_unmounted_subtitle)
                        }
                        workingDirectory.lastAccessed != null -> {
                            stringResource(R.string.working_directory_last_accessed, workingDirectory.lastAccessed.formatMediumDateTime())
                        }
                        else -> {
                            stringResource(R.string.working_directory_never_accessed)
                        }
                    },
                )
            },
        )
        HorizontalDivider()
    }
}
