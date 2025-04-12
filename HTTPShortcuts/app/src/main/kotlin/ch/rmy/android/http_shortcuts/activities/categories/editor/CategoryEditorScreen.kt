package ch.rmy.android.http_shortcuts.activities.categories.editor

import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.BackButton
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.ToolbarIcon
import ch.rmy.android.http_shortcuts.components.bindViewModel
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId

@Composable
fun CategoryEditorScreen(categoryId: CategoryId?) {
    val (viewModel, state) = bindViewModel<CategoryEditorViewModel.InitData, CategoryEditorViewState, CategoryEditorViewModel>(
        CategoryEditorViewModel.InitData(categoryId),
    )

    BackHandler(enabled = state?.hasChanges == true) {
        viewModel.onBackPressed()
    }

    SimpleScaffold(
        viewState = state,
        title = stringResource(if (categoryId == null) R.string.title_create_category else R.string.title_edit_category),
        backButton = BackButton.CROSS,
        actions = { viewState ->
            ToolbarIcon(
                Icons.Filled.Check,
                contentDescription = stringResource(R.string.save_button),
                enabled = viewState.saveButtonEnabled,
                onClick = viewModel::onSaveButtonClicked,
            )
        },
    ) { viewState ->
        CategoryEditorContent(
            colorButtonVisible = viewState.colorButtonVisible,
            categoryName = viewState.categoryName,
            categoryLayoutType = viewState.categoryLayoutType,
            categoryBackgroundType = viewState.categoryBackground,
            backgroundColor = viewState.backgroundColor,
            backgroundColorAsText = viewState.backgroundColorAsText,
            selectedClickActionOption = viewState.categoryClickBehavior,
            scale = viewState.scale,
            onCategoryNameChanged = viewModel::onCategoryNameChanged,
            onLayoutTypeSelected = viewModel::onLayoutTypeChanged,
            onBackgroundTypeSelected = viewModel::onBackgroundChanged,
            onColorButtonClicked = viewModel::onColorButtonClicked,
            onClickActionOptionSelected = viewModel::onClickBehaviorChanged,
            onScaleChanged = viewModel::onScaleChanged,
        )
    }

    CategoryEditorDialogs(
        state?.dialogState,
        onColorSelected = viewModel::onBackgroundColorSelected,
        onDiscardConfirmed = viewModel::onDiscardConfirmed,
        onDismissRequested = viewModel::onDialogDismissalRequested,
    )
}
