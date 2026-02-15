package ch.rmy.android.http_shortcuts.activities.sync

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.sync.components.DirectoryButton
import ch.rmy.android.http_shortcuts.activities.sync.components.PasswordProtection
import ch.rmy.android.http_shortcuts.activities.sync.components.SyncScheduleSelector
import ch.rmy.android.http_shortcuts.activities.sync.models.SyncCategory
import ch.rmy.android.http_shortcuts.components.HelpText
import ch.rmy.android.http_shortcuts.components.SelectionField
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.components.VerticalSpacer
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.enums.SyncSchedule
import ch.rmy.android.http_shortcuts.data.enums.SyncTargetType

@Composable
fun SyncExportContent(
    viewState: SyncExportViewState,
    onSyncCategoryChecked: (CategoryId, Boolean) -> Unit,
    onScheduleChanged: (SyncSchedule) -> Unit,
    onFilePasswordChanged: (String) -> Unit,
    onTargetTypeChanged: (SyncTargetType) -> Unit,
    onDirectoryClicked: () -> Unit,
    onFileNameChanged: (String) -> Unit,
    onWebUrlChanged: (String) -> Unit,
    onWebAuthUsernameChanged: (String) -> Unit,
    onWebAuthPasswordChanged: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.MEDIUM)
            .verticalScroll(rememberScrollState()),
    ) {
        if (viewState.categories.size > 1) {
            Text(
                text = stringResource(R.string.title_categories),
                style = MaterialTheme.typography.labelMedium,
            )

            val isSingle = viewState.categories.count { it.checked } == 1
            viewState.categories.forEach { category ->
                CategoryItem(
                    enabled = !category.checked || !isSingle,
                    category = category,
                    onChecked = { checked ->
                        onSyncCategoryChecked(category.id, checked)
                    },
                )
            }

            VerticalSpacer(Spacing.MEDIUM)

            HorizontalDivider()

            VerticalSpacer(Spacing.MEDIUM)
        }

        SyncScheduleSelector(
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(R.string.label_export_schedule),
            syncSchedule = viewState.schedule,
            onSyncScheduleChanged = onScheduleChanged,
        )

        VerticalSpacer(Spacing.MEDIUM)

        HorizontalDivider()

        VerticalSpacer(Spacing.MEDIUM)

        PasswordProtection(
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(R.string.label_export_protect_with_password),
            password = viewState.filePassword,
            onPasswordChanged = onFilePasswordChanged,
        )

        VerticalSpacer(Spacing.MEDIUM)

        HorizontalDivider()

        VerticalSpacer(Spacing.MEDIUM)

        SelectionField(
            title = stringResource(R.string.label_sync_export_target),
            selectedKey = viewState.targetType,
            items = listOf(
                SyncTargetType.FILE to stringResource(R.string.option_sync_target_file),
                SyncTargetType.URL to stringResource(R.string.option_sync_target_url),
            ),
            onItemSelected = onTargetTypeChanged,
        )

        AnimatedVisibility(visible = viewState.targetType == SyncTargetType.FILE) {
            Column {
                VerticalSpacer(Spacing.MEDIUM)

                HelpText(stringResource(R.string.instructions_sync_export_file))

                VerticalSpacer(Spacing.MEDIUM)

                DirectoryButton(
                    directoryName = viewState.directoryName,
                    onClick = onDirectoryClicked,
                )

                VerticalSpacer(Spacing.SMALL)

                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            text = stringResource(R.string.label_sync_file_name),
                        )
                    },
                    value = viewState.fileName,
                    onValueChange = onFileNameChanged,
                    singleLine = true,
                )
            }
        }

        AnimatedVisibility(visible = viewState.targetType == SyncTargetType.URL) {
            Column {
                VerticalSpacer(Spacing.MEDIUM)

                HelpText(stringResource(R.string.instructions_sync_export_web))

                VerticalSpacer(Spacing.MEDIUM)

                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            text = stringResource(R.string.label_url),
                        )
                    },
                    value = viewState.webUrl,
                    onValueChange = onWebUrlChanged,
                    singleLine = true,
                )

                VerticalSpacer(Spacing.SMALL)

                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            text = stringResource(R.string.label_username),
                        )
                    },
                    value = viewState.webAuthUsername,
                    onValueChange = onWebAuthUsernameChanged,
                    singleLine = true,
                )

                VerticalSpacer(Spacing.SMALL)

                PasswordProtection(
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(R.string.label_password),
                    password = viewState.webAuthPassword,
                    onPasswordChanged = onWebAuthPasswordChanged,
                )
            }
        }

        VerticalSpacer(Spacing.TINY)
    }
}

@Composable
private fun CategoryItem(
    modifier: Modifier = Modifier,
    category: SyncCategory,
    enabled: Boolean,
    onChecked: (Boolean) -> Unit,
) {
    Row(
        modifier = modifier
            .toggleable(
                enabled = enabled,
                role = Role.Checkbox,
                value = category.checked,
                onValueChange = onChecked,
            )
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.TINY),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            modifier = Modifier.padding(Spacing.TINY),
            enabled = enabled,
            checked = category.checked,
            onCheckedChange = null,
        )
        Text(
            modifier = Modifier.padding(vertical = Spacing.SMALL),
            text = category.name,
        )
    }
}
