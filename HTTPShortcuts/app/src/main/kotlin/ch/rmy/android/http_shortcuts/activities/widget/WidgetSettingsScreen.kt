package ch.rmy.android.http_shortcuts.activities.widget

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.BackButton
import ch.rmy.android.http_shortcuts.components.ColorPickerDialog
import ch.rmy.android.http_shortcuts.components.ScreenScope
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.ToolbarIcon
import ch.rmy.android.http_shortcuts.components.bindViewModel
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

@Composable
fun ScreenScope.WidgetSettingsScreen(
    shortcutId: ShortcutId,
    shortcutName: String,
    shortcutIcon: ShortcutIcon,
) {
    val (viewModel, state) = bindViewModel<WidgetSettingsViewModel.InitData, WidgetSettingsViewState, WidgetSettingsViewModel>(
        WidgetSettingsViewModel.InitData(shortcutId, shortcutName, shortcutIcon)
    )

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.title_configure_widget),
        backButton = BackButton.CROSS,
        actions = {
            ToolbarIcon(
                Icons.Filled.Check,
                contentDescription = stringResource(R.string.action_create_widget),
                onClick = viewModel::onCreateButtonClicked,
            )
        }
    ) { viewState ->
        WidgetSettingsContent(
            showLabel = viewState.showLabel,
            labelColor = Color(viewState.labelColor),
            labelColorText = viewState.labelColorFormatted,
            shortcutName = viewState.shortcutName,
            shortcutIcon = viewState.shortcutIcon,
            onShowLabelChanged = viewModel::onShowLabelChanged,
            onLabelColorButtonClicked = viewModel::onLabelColorButtonClicked,
        )
    }

    if (state?.colorDialogVisible == true) {
        ColorPickerDialog(
            initialColor = state.labelColor,
            onColorSelected = viewModel::onLabelColorSelected,
            onDismissRequested = viewModel::onDialogDismissalRequested,
        )
    }
}
