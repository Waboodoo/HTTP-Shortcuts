package ch.rmy.android.http_shortcuts.activities.categories.sections

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.FloatingAddButton
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.bindViewModel
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId

@Composable
fun CategorySectionsScreen(categoryId: CategoryId) {
    val (viewModel, state) = bindViewModel<CategorySectionsViewModel.InitData, CategorySectionsViewState, CategorySectionsViewModel>(
        CategorySectionsViewModel.InitData(categoryId),
    )

    BackHandler(state != null) {
        viewModel.onBackPressed()
    }

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.screen_title_category_sections),
        floatingActionButton = {
            FloatingAddButton(
                onClick = viewModel::onAddSectionButtonClicked,
                contentDescription = stringResource(R.string.accessibility_label_add_category_section),
            )
        },
    ) { viewState ->
        CategorySectionsContent(
            sections = viewState.sectionItems,
            onSectionClicked = viewModel::onSectionClicked,
            onSectionMoved = viewModel::onSectionMoved,
        )
    }

    CategorySectionsDialogs(
        dialogState = state?.dialogState,
        onConfirmed = viewModel::onDialogConfirmed,
        onDelete = viewModel::onRemoveSectionButtonClicked,
        onDismissed = viewModel::onDismissDialog,
    )
}
