package ch.rmy.android.http_shortcuts.activities.sync

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.sync.models.SyncState
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.components.VerticalSpacer
import ch.rmy.android.http_shortcuts.data.enums.SyncType
import ch.rmy.android.http_shortcuts.extensions.formatMediumDateTime
import java.time.LocalDateTime

@Composable
fun SyncOverviewContent(
    viewState: SyncOverviewViewState,
    onSyncTypeSelected: (SyncType?) -> Unit,
    onConfigureImportClicked: () -> Unit,
    onConfigureExportClicked: () -> Unit,
    onFailureInfoClicked: () -> Unit,
) {
    val isEnabled = viewState.syncState != SyncState.SYNCING
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.MEDIUM)
            .verticalScroll(rememberScrollState()),
    ) {
        VerticalSpacer(height = 1.dp)

        SyncTypeCard(
            enabled = isEnabled,
            title = stringResource(R.string.sync_type_disabled),
            selected = viewState.syncType == null,
            onClick = {
                onSyncTypeSelected(null)
            },
        )

        VerticalSpacer(height = Spacing.MEDIUM)

        SyncTypeCard(
            enabled = isEnabled,
            title = stringResource(R.string.sync_type_automatic_import),
            description = stringResource(R.string.sync_type_automatic_import_description),
            selected = viewState.syncType == SyncType.IMPORT,
            onClick = {
                onSyncTypeSelected(SyncType.IMPORT)
            },
        ) {
            LastSuccessOrFailure(
                enabled = viewState.syncType == SyncType.IMPORT && isEnabled,
                lastSucceeded = viewState.importLastSucceeded,
                lastFailed = viewState.importLastFailed,
                onInfoClicked = onFailureInfoClicked,
            )

            Button(
                onClick = onConfigureImportClicked,
                enabled = viewState.syncType == SyncType.IMPORT && isEnabled,
            ) {
                Text(stringResource(R.string.button_sync_configure))
            }
        }

        VerticalSpacer(height = Spacing.MEDIUM)

        SyncTypeCard(
            enabled = isEnabled,
            title = stringResource(R.string.sync_type_automatic_export),
            description = stringResource(R.string.sync_type_automatic_export_description),
            selected = viewState.syncType == SyncType.EXPORT,
            onClick = {
                onSyncTypeSelected(SyncType.EXPORT)
            },
        ) {
            LastSuccessOrFailure(
                enabled = viewState.syncType == SyncType.EXPORT && isEnabled,
                lastSucceeded = viewState.exportLastSucceeded,
                lastFailed = viewState.exportLastFailed,
                onInfoClicked = onFailureInfoClicked,
            )

            Button(
                onClick = onConfigureExportClicked,
                enabled = viewState.syncType == SyncType.EXPORT && isEnabled,
            ) {
                Text(stringResource(R.string.button_sync_configure))
            }
        }

        VerticalSpacer(height = Spacing.MEDIUM)
    }
}

@Composable
private fun LastSuccessOrFailure(
    enabled: Boolean,
    lastSucceeded: LocalDateTime?,
    lastFailed: LocalDateTime?,
    onInfoClicked: () -> Unit,
) {
    AnimatedVisibility(
        lastSucceeded != null || lastFailed != null,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 24.dp),
    ) {
        if (lastSucceeded != null && (lastFailed == null || lastSucceeded > lastFailed)) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 24.dp),
                text = stringResource(R.string.label_sync_last_success_pattern, lastSucceeded.formatMediumDateTime()),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            )
        } else if (lastFailed != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        enabled = enabled,
                        onClick = onInfoClicked,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.TINY),
            ) {
                val color = if (enabled) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                }
                Icon(
                    painter = painterResource(R.drawable.outline_info_24),
                    contentDescription = stringResource(R.string.settings_troubleshooting),
                    tint = color,
                )
                Text(
                    text = stringResource(R.string.label_sync_last_failure_pattern, lastFailed.formatMediumDateTime()),
                    color = color,
                )
            }
        }
    }
}

@Composable
private fun SyncTypeCard(
    enabled: Boolean,
    title: String,
    description: String? = null,
    selected: Boolean,
    onClick: () -> Unit,
    content: (@Composable () -> Unit)? = null,
) {
    OutlinedCard(
        enabled = enabled,
        onClick = onClick,
    ) {
        ListItem(
            leadingContent = {
                RadioButton(
                    enabled = enabled,
                    selected = selected,
                    onClick = null,
                    modifier = Modifier.clearAndSetSemantics {},
                )
            },
            headlineContent = {
                Text(
                    text = title,
                )
            },
            supportingContent = if (description != null || content != null) {
                {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(Spacing.SMALL),
                    ) {
                        if (description != null) {
                            Text(
                                text = description,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        if (content != null) {
                            content()
                        }
                    }
                }
            } else {
                null
            },
        )
    }
}

@Preview
@Composable
private fun SyncContent_Preview() {
    SyncOverviewContent(
        viewState = SyncOverviewViewState(
            syncType = SyncType.IMPORT,
            isConfigValid = true,
            isSyncing = false,
            importLastSucceeded = null,
            importLastFailed = LocalDateTime.now(),
            exportLastSucceeded = null,
            exportLastFailed = null,
        ),
        onSyncTypeSelected = {},
        onConfigureImportClicked = {},
        onConfigureExportClicked = {},
        onFailureInfoClicked = {},
    )
}
