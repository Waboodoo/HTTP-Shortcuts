package ch.rmy.android.http_shortcuts.activities.variables.editor

import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.SavedStateHandle
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.variables.VariableTypeMappings.getTypeName
import ch.rmy.android.http_shortcuts.components.BackButton
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.ToolbarIcon
import ch.rmy.android.http_shortcuts.components.bindViewModel
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.enums.VariableType

@Composable
fun VariableEditorScreen(
    savedStateHandle: SavedStateHandle,
    variableId: VariableId?,
    variableType: VariableType,
) {
    val (viewModel, state) = bindViewModel<VariableEditorViewModel.InitData, VariableEditorViewState, VariableEditorViewModel>(
        VariableEditorViewModel.InitData(variableId, variableType),
    )

    BackHandler(state != null) {
        viewModel.onBackPressed()
    }

    SimpleScaffold(
        viewState = state,
        title = stringResource(if (variableId == null) R.string.create_variable else R.string.edit_variable),
        subtitle = stringResource(variableType.getTypeName()),
        backButton = BackButton.CROSS,
        actions = {
            ToolbarIcon(
                Icons.Filled.Check,
                contentDescription = stringResource(R.string.save_button),
                onClick = viewModel::onSaveButtonClicked,
            )
        },
    ) { viewState ->
        VariableEditorContent(
            variableKey = viewState.variableKey,
            dialogTitle = viewState.dialogTitle,
            dialogMessage = viewState.dialogMessage,
            urlEncodeChecked = viewState.urlEncodeChecked,
            jsonEncodeChecked = viewState.jsonEncodeChecked,
            allowShareChecked = viewState.allowShareChecked,
            shareSupport = viewState.shareSupport,
            variableKeyInputError = viewState.variableKeyInputError,
            dialogTitleVisible = viewState.dialogTitleVisible,
            dialogMessageVisible = viewState.dialogMessageVisible,
            shareSupportVisible = viewState.shareSupportVisible,
            excludeValueCheckboxVisible = viewState.excludeValueCheckboxVisible,
            excludeValueFromExports = viewState.excludeValueFromExports,
            onVariableKeyChanged = viewModel::onVariableKeyChanged,
            onDialogTitleChanged = viewModel::onDialogTitleChanged,
            onDialogMessageChanged = viewModel::onDialogMessageChanged,
            onUrlEncodeChanged = viewModel::onUrlEncodeChanged,
            onJsonEncodeChanged = viewModel::onJsonEncodeChanged,
            onAllowShareChanged = viewModel::onAllowShareChanged,
            onShareSupportChanged = viewModel::onShareSupportChanged,
            onExcludeValueFromExportsChanged = viewModel::onExcludeValueFromExportsChanged,
        ) {
            VariableTypeSpecificContent(
                savedStateHandle,
                viewState.variableTypeViewState,
                onViewStateChanged = viewModel::onVariableTypeViewStateChanged,
            )
        }
    }

    VariableEditorDialogs(
        dialogState = state?.dialogState,
        onDiscardDialogConfirmed = viewModel::onDiscardDialogConfirmed,
        onDismissed = viewModel::onDismissDialog,
    )
}
