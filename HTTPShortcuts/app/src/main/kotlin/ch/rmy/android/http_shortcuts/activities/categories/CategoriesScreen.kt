package ch.rmy.android.http_shortcuts.activities.categories

import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.SavedStateHandle
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.FloatingAddButton
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.ToolbarIcon
import ch.rmy.android.http_shortcuts.components.bindViewModel
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination.CategoryEditor.RESULT_CATEGORY_CREATED
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination.CategoryEditor.RESULT_CATEGORY_EDITED
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination.Companion.RESULT_CHANGES_DISCARDED
import ch.rmy.android.http_shortcuts.navigation.ResultHandler

@Composable
fun CategoriesScreen(savedStateHandle: SavedStateHandle) {
    val (viewModel, state) = bindViewModel<CategoriesViewState, CategoriesViewModel>()

    ResultHandler(savedStateHandle) { result ->
        when (result) {
            RESULT_CATEGORY_CREATED -> viewModel.onCategoryCreated()
            RESULT_CATEGORY_EDITED -> viewModel.onCategoryEdited()
            RESULT_CHANGES_DISCARDED -> viewModel.onChangesDiscarded()
            is NavigationDestination.IconPicker.Result -> {
                viewModel.onCategoryIconSelected(result.icon)
            }
        }
    }

    BackHandler(state != null) {
        viewModel.onBackPressed()
    }

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.title_categories),
        actions = {
            ToolbarIcon(
                Icons.AutoMirrored.Filled.HelpOutline,
                contentDescription = stringResource(R.string.button_show_help),
                onClick = viewModel::onHelpButtonClicked,
            )
        },
        floatingActionButton = {
            FloatingAddButton(
                onClick = viewModel::onCreateCategoryButtonClicked,
                stringResource(R.string.accessibility_label_create_category_fab),
            )
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
        onManageSectionsClicked = viewModel::onManageSectionsClicked,
        onVisibilityChangeRequested = viewModel::onCategoryVisibilityChanged,
        onPlaceOnHomeScreenClicked = viewModel::onPlaceOnHomeScreenClicked,
        onDeleteClicked = viewModel::onDeleteClicked,
        onDeletionConfirmed = viewModel::onCategoryDeletionConfirmed,
        onIconSelected = viewModel::onCategoryIconSelected,
        onCustomIconOptionSelected = viewModel::onCustomIconOptionSelected,
        onDismissRequested = viewModel::onDialogDismissed,
    )
}
