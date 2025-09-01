package ch.rmy.android.http_shortcuts.activities.importexport

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.components.VerticalSpacer
import ch.rmy.android.http_shortcuts.import_export.Importer

@Composable
fun ImportExportContent(
    importStatus: Importer.ImportStatus?,
    onImportFromFileClicked: () -> Unit,
    onExportToFileClicked: () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(horizontal = Spacing.MEDIUM)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        VerticalSpacer(Spacing.HUGE)

        Text(
            text = stringResource(R.string.import_instructions),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = if (isSystemInDarkTheme()) {
                Color.White
            } else {
                Color.Black
            },
        )

        VerticalSpacer(Spacing.BIG)
        Button(
            onClick = onImportFromFileClicked,
        ) {
            Text(stringResource(R.string.import_from_file))
        }
        if (importStatus != null) {
            Text(
                text = pluralStringResource(
                    R.plurals.shortcut_import_success,
                    importStatus.importedShortcuts,
                    importStatus.importedShortcuts,
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = if (isSystemInDarkTheme()) {
                    Color.White
                } else {
                    Color.Black
                },
            )

            VerticalSpacer(Spacing.MEDIUM)
            Button(
                onClick = onExportToFileClicked,
            ) {
                Text(stringResource(R.string.export_to_file))
            }
        }

        VerticalSpacer(Spacing.MEDIUM)
    }
}
