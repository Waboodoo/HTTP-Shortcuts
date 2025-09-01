package ch.rmy.android.http_shortcuts.activities.sync

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.components.VerticalSpacer
import ch.rmy.android.http_shortcuts.data.enums.SyncType

@Composable
fun SyncOverviewContent(
    viewState: SyncOverviewViewState,
    onSyncTypeSelected: (SyncType?) -> Unit,
    onConfigureImportClicked: () -> Unit,
    onConfigureExportClicked: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.MEDIUM)
            .verticalScroll(rememberScrollState()),
    ) {
        VerticalSpacer(height = 1.dp)

        SyncTypeCard(
            title = stringResource(R.string.sync_type_disabled),
            selected = viewState.syncType == null,
            onClick = {
                onSyncTypeSelected(null)
            },
        )

        VerticalSpacer(height = Spacing.MEDIUM)

        SyncTypeCard(
            title = stringResource(R.string.sync_type_automatic_import),
            description = stringResource(R.string.sync_type_automatic_import_description),
            selected = viewState.syncType == SyncType.IMPORT,
            onClick = {
                onSyncTypeSelected(SyncType.IMPORT)
            },
        ) {
            Button(
                onClick = onConfigureImportClicked,
                enabled = viewState.syncType == SyncType.IMPORT,
            ) {
                Text(stringResource(R.string.button_sync_configure))
            }
        }

        VerticalSpacer(height = Spacing.MEDIUM)

        SyncTypeCard(
            title = stringResource(R.string.sync_type_automatic_export),
            description = stringResource(R.string.sync_type_automatic_export_description),
            selected = viewState.syncType == SyncType.EXPORT,
            onClick = {
                onSyncTypeSelected(SyncType.EXPORT)
            },
        ) {
            Button(
                onClick = onConfigureExportClicked,
                enabled = viewState.syncType == SyncType.EXPORT,
            ) {
                Text(stringResource(R.string.button_sync_configure))
            }
        }

        VerticalSpacer(height = Spacing.MEDIUM)
    }
}

@Composable
private fun SyncTypeCard(
    title: String,
    description: String? = null,
    selected: Boolean,
    onClick: () -> Unit,
    content: (@Composable () -> Unit)? = null,
) {
    OutlinedCard(
        onClick = onClick,
    ) {
        ListItem(
            leadingContent = {
                RadioButton(
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
        ),
        onSyncTypeSelected = {},
        onConfigureImportClicked = {},
        onConfigureExportClicked = {},
    )
}
