package ch.rmy.android.http_shortcuts.activities.variables

import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.FloatingAddButton
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.ToolbarIcon
import ch.rmy.android.http_shortcuts.components.bindViewModel

@Composable
fun VariablesScreen() {
    val (viewModel, state) = bindViewModel<VariablesViewState, VariablesViewModel>()

    BackHandler(state != null) {
        viewModel.onBackPressed()
    }

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.title_variables),
        actions = { viewState ->
            ToolbarIcon(
                Icons.AutoMirrored.Filled.Sort,
                contentDescription = stringResource(R.string.button_sort_variables),
                enabled = viewState.isSortButtonEnabled,
                onClick = viewModel::onSortButtonClicked,
            )
            ToolbarIcon(
                Icons.AutoMirrored.Filled.HelpOutline,
                contentDescription = stringResource(R.string.button_show_help),
                onClick = viewModel::onHelpButtonClicked,
            )
        },
        floatingActionButton = {
            FloatingAddButton(onClick = viewModel::onCreateButtonClicked)
        },
    ) { viewState ->
        VariablesContent(
            variables = viewState.variables,
            onVariableClicked = viewModel::onVariableClicked,
            onVariableMoved = viewModel::onVariableMoved,
        )
    }

    VariablesDialogs(
        state?.dialogState,
        onVariableTypeSelected = viewModel::onCreationDialogVariableTypeSelected,
        onEditClicked = viewModel::onEditOptionSelected,
        onDuplicateClicked = viewModel::onDuplicateOptionSelected,
        onDeleteClicked = viewModel::onDeletionOptionSelected,
        onDeleteConfirmed = viewModel::onDeletionConfirmed,
        onDismissed = viewModel::onDialogDismissed,
    )
}
