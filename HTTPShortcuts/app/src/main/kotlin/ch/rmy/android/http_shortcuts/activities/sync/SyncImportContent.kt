package ch.rmy.android.http_shortcuts.activities.sync

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.sync.components.DirectoryButton
import ch.rmy.android.http_shortcuts.activities.sync.components.PasswordProtection
import ch.rmy.android.http_shortcuts.activities.sync.components.SyncScheduleSelector
import ch.rmy.android.http_shortcuts.components.ConfirmDialog
import ch.rmy.android.http_shortcuts.components.HelpText
import ch.rmy.android.http_shortcuts.components.SelectionField
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.components.VerticalSpacer
import ch.rmy.android.http_shortcuts.data.enums.SyncSchedule
import ch.rmy.android.http_shortcuts.data.enums.SyncTargetType

@Composable
fun SyncImportContent(
    viewState: SyncImportViewState,
    onScheduleChanged: (SyncSchedule) -> Unit,
    onFilePasswordChanged: (String) -> Unit,
    onTargetTypeChanged: (SyncTargetType) -> Unit,
    onDirectoryClicked: () -> Unit,
    onFileNameChanged: (String) -> Unit,
    onWebUrlChanged: (String) -> Unit,
    onWebAuthUsernameChanged: (String) -> Unit,
    onWebAuthPasswordChanged: (String) -> Unit,
    onReplaceLocalChanged: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.MEDIUM)
            .verticalScroll(rememberScrollState()),
    ) {
        var showReplaceModeWarning by rememberSaveable {
            mutableStateOf(false)
        }
        var replaceModeWarningShown by rememberSaveable {
            mutableStateOf(viewState.replaceLocal)
        }

        if (showReplaceModeWarning) {
            ConfirmDialog(
                title = stringResource(R.string.warning_dialog_title),
                message = stringResource(R.string.warning_sync_import_mode_replace, stringResource(R.string.option_sync_import_mode_replace)),
                confirmButton = stringResource(R.string.dialog_ok),
                onConfirmRequest = {
                    showReplaceModeWarning = false
                    replaceModeWarningShown = true
                    onReplaceLocalChanged(true)
                },
                onDismissRequest = {
                    showReplaceModeWarning = false
                },
            )
        }

        SelectionField(
            title = stringResource(R.string.label_sync_import_mode),
            selectedKey = viewState.replaceLocal,
            items = listOf(
                false to stringResource(R.string.option_sync_import_mode_merge),
                true to stringResource(R.string.option_sync_import_mode_replace),
            ),
            onItemSelected = { enableReplaceMode ->
                if (enableReplaceMode) {
                    if (replaceModeWarningShown) {
                        onReplaceLocalChanged(true)
                    } else if (!viewState.replaceLocal) {
                        showReplaceModeWarning = true
                    }
                } else {
                    showReplaceModeWarning = false
                    onReplaceLocalChanged(false)
                }
            },
        )

        VerticalSpacer(Spacing.MEDIUM)

        HorizontalDivider()

        VerticalSpacer(Spacing.MEDIUM)

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
            label = stringResource(R.string.label_import_protected_with_password),
            password = viewState.filePassword,
            onPasswordChanged = onFilePasswordChanged,
        )

        VerticalSpacer(Spacing.MEDIUM)

        HorizontalDivider()

        VerticalSpacer(Spacing.MEDIUM)

        SelectionField(
            title = stringResource(R.string.label_sync_import_target),
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

                HelpText(stringResource(R.string.instructions_sync_import_file))

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

                HelpText(stringResource(R.string.instructions_sync_import_web))

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
