package ch.rmy.android.http_shortcuts.activities.editor.typepicker

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.BackButton
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.ToolbarIcon
import ch.rmy.android.http_shortcuts.components.bindViewModel
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId

@Composable
fun TypePickerScreen(
    categoryId: CategoryId,
) {
    val (viewModel, _) = bindViewModel<TypePickerViewModel.InitData, Unit, TypePickerViewModel>(
        TypePickerViewModel.InitData(categoryId),
    )

    SimpleScaffold(
        viewState = Unit,
        title = stringResource(R.string.create_shortcut),
        backButton = BackButton.ARROW,
        actions = {
            ToolbarIcon(
                Icons.AutoMirrored.Filled.HelpOutline,
                contentDescription = stringResource(R.string.button_show_help),
                onClick = viewModel::onHelpButtonClicked,
            )
        },
    ) {
        TypePickerContent(
            onShortcutTypeSelected = viewModel::onCreationDialogOptionSelected,
            onCurlImportSelected = viewModel::onCurlImportOptionSelected,
        )
    }
}
