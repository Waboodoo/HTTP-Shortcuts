package ch.rmy.android.http_shortcuts.activities.categories

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.launch
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.categories.editor.CategoryEditorActivity
import ch.rmy.android.http_shortcuts.components.FloatingAddButton
import ch.rmy.android.http_shortcuts.components.ScreenScope
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.ToolbarIcon
import ch.rmy.android.http_shortcuts.components.bindViewModel

@Composable
fun ScreenScope.CategoriesScreen() {
    val (viewModel, state) = bindViewModel<CategoriesViewState, CategoriesViewModel>()

    val openCategoryEditorForCreation = rememberLauncherForActivityResult(CategoryEditorActivity.OpenCategoryEditor) { success ->
        if (success) {
            viewModel.onCategoryCreated()
        }
    }
    val openCategoryEditorForEditing = rememberLauncherForActivityResult(CategoryEditorActivity.OpenCategoryEditor) { success ->
        if (success) {
            viewModel.onCategoryEdited()
        }
    }

    EventHandler { event ->
        when (event) {
            is CategoriesEvent.OpenCategoryEditor -> consume {
                if (event.categoryId != null) {
                    openCategoryEditorForEditing.launch { categoryId(event.categoryId) }
                } else {
                    openCategoryEditorForCreation.launch()
                }
            }

            else -> false
        }
    }

    BackHandler {
        viewModel.onBackPressed()
    }

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.title_categories),
        actions = {
            ToolbarIcon(
                Icons.Filled.HelpOutline,
                contentDescription = stringResource(R.string.button_show_help),
                onClick = viewModel::onHelpButtonClicked,
            )
        },
        floatingActionButton = {
            FloatingAddButton(onClick = viewModel::onCreateCategoryButtonClicked)
        },
    ) { viewState ->
        CategoriesContent(
            categories = viewState.categories,
            onCategoryClicked = viewModel::onCategoryClicked,
            onCategoryMoved = viewModel::onCategoryMoved,
        )
    }

    CategoriesDialogs(
        state?.dialogState,
        onEditClicked = viewModel::onEditCategoryOptionSelected,
        onVisibilityChangeRequested = viewModel::onCategoryVisibilityChanged,
        onPlaceOnHomeScreenClicked = viewModel::onPlaceOnHomeScreenClicked,
        onDeleteClicked = viewModel::onDeleteClicked,
        onDeletionConfirmed = viewModel::onCategoryDeletionConfirmed,
        onIconSelected = viewModel::onCategoryIconSelected,
        onDismissRequested = viewModel::onDialogDismissed,
    )
}
