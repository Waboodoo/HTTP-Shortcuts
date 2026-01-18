package ch.rmy.android.http_shortcuts.activities.importexport

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.SettingsButton
import ch.rmy.android.http_shortcuts.components.SettingsGroup

@Composable
fun ImportExportContent(
    exportEnabled: Boolean,
    onImportFromFileClicked: () -> Unit,
    onImportFromUrlClicked: () -> Unit,
    onExportToFileClicked: () -> Unit,
    onExportViaShareClicked: () -> Unit,
    onRemoteEditButtonClicked: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
        SettingsGroup(
            title = stringResource(R.string.settings_title_import),
        ) {
            SettingsButton(
                icon = painterResource(R.drawable.outline_input_24),
                title = stringResource(R.string.settings_import_from_file),
                onClick = onImportFromFileClicked,
            )

            SettingsButton(
                icon = painterResource(R.drawable.outline_cloud_download_24),
                title = stringResource(R.string.settings_import_from_url),
                onClick = onImportFromUrlClicked,
            )
        }

        SettingsGroup(
            title = stringResource(R.string.settings_title_export),
        ) {
            SettingsButton(
                icon = painterResource(R.drawable.outline_output_24),
                title = stringResource(R.string.settings_export_to_file),
                enabled = exportEnabled,
                onClick = onExportToFileClicked,
            )
            SettingsButton(
                icon = painterResource(R.drawable.outline_share_24),
                title = stringResource(R.string.settings_export_via_share),
                enabled = exportEnabled,
                onClick = onExportViaShareClicked,
            )
        }

        SettingsGroup(
            title = stringResource(R.string.settings_title_remote_edit),
        ) {
            SettingsButton(
                icon = painterResource(R.drawable.outline_devices_24),
                title = stringResource(R.string.settings_remote_edit),
                onClick = onRemoteEditButtonClicked,
            )
        }
    }
}
