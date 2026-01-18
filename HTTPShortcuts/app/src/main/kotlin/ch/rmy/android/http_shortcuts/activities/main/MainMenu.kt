package ch.rmy.android.http_shortcuts.activities.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
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
            icon = painterResource(R.drawable.outline_view_week_24),
            onClick = onCategoriesButtonClicked,
        )
        MenuItem(
            title = stringResource(R.string.title_variables),
            icon = painterResource(R.drawable.outline_data_object_24),
            onClick = onVariablesButtonClicked,
        )
        MenuItem(
            title = stringResource(R.string.menu_action_working_directories),
            icon = painterResource(R.drawable.outline_folder_24),
            onClick = onWorkingDirectoriesClicked,
        )
        MenuItem(
            title = stringResource(R.string.title_import_export),
            icon = painterResource(R.drawable.outline_compare_arrows_24),
            onClick = onImportExportButtonClicked,
        )
        MenuItem(
            title = stringResource(R.string.settings_troubleshooting),
            icon = painterResource(R.drawable.outline_troubleshoot_24),
            onClick = onTroubleShootingButtonClicked,
        )
        MenuItem(
            title = stringResource(R.string.title_settings),
            icon = painterResource(R.drawable.outline_settings_24),
            onClick = onSettingsButtonClicked,
        )
        MenuItem(
            title = stringResource(R.string.title_about),
            icon = painterResource(R.drawable.outline_info_24),
            onClick = onAboutButtonClicked,
        )
    }
}
