package ch.rmy.android.http_shortcuts.activities.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Troubleshoot
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.Menu
import ch.rmy.android.http_shortcuts.components.MenuItem

@Composable
fun MainMenu(
    onCategoriesButtonClicked: () -> Unit,
    onVariablesButtonClicked: () -> Unit,
    onWorkingDirectoriesClicked: () -> Unit,
    onImportExportButtonClicked: () -> Unit,
    onTroubleShootingButtonClicked: () -> Unit,
    onSettingsButtonClicked: () -> Unit,
    onAboutButtonClicked: () -> Unit,
) {
    Menu {
        MenuItem(
            title = stringResource(R.string.title_categories),
            icon = Icons.Filled.ViewWeek,
            onClick = onCategoriesButtonClicked,
        )
        MenuItem(
            title = stringResource(R.string.title_variables),
            icon = Icons.Filled.DataObject,
            onClick = onVariablesButtonClicked,
        )
        MenuItem(
            title = stringResource(R.string.menu_action_working_directories),
            icon = Icons.Filled.Folder,
            onClick = onWorkingDirectoriesClicked,
        )
        MenuItem(
            title = stringResource(R.string.title_import_export),
            icon = Icons.Filled.ImportExport,
            onClick = onImportExportButtonClicked,
        )
        MenuItem(
            title = stringResource(R.string.settings_troubleshooting),
            icon = Icons.Filled.Troubleshoot,
            onClick = onTroubleShootingButtonClicked,
        )
        MenuItem(
            title = stringResource(R.string.title_settings),
            icon = Icons.Filled.Settings,
            onClick = onSettingsButtonClicked,
        )
        MenuItem(
            title = stringResource(R.string.title_about),
            icon = Icons.Filled.Info,
            onClick = onAboutButtonClicked,
        )
    }
}
