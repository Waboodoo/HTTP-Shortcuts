package ch.rmy.android.http_shortcuts.activities.variables.editor

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.SavedStateHandle
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.variables.VariableTypeMappings.getTypeName
import ch.rmy.android.http_shortcuts.components.BackButton
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.ToolbarIcon
import ch.rmy.android.http_shortcuts.components.bindViewModel
import ch.rmy.android.http_shortcuts.data.domains.variables.GlobalVariableId
import ch.rmy.android.http_shortcuts.data.enums.VariableType

@Composable
fun GlobalVariableEditorScreen(
    savedStateHandle: SavedStateHandle,
    globalVariableId: GlobalVariableId?,
    variableType: VariableType,
) {
    val (viewModel, state) = bindViewModel<GlobalVariableEditorViewModel.InitData, GlobalVariableEditorViewState, GlobalVariableEditorViewModel>(
        GlobalVariableEditorViewModel.InitData(globalVariableId, variableType),
    )

    BackHandler(state != null) {
        viewModel.onBackPressed()
    }

    SimpleScaffold(
        viewState = state,
        title = stringResource(if (globalVariableId == null) R.string.create_variable else R.string.edit_variable),
        subtitle = stringResource(variableType.getTypeName()),
        backButton = BackButton.CROSS,
        actions = {
            ToolbarIcon(
                painterResource(R.drawable.outline_help_24),
                contentDescription = stringResource(R.string.button_show_help),
                onClick = viewModel::onHelpButtonClicked,
            )
            ToolbarIcon(
                painterResource(R.drawable.outline_check_24),
                contentDescription = stringResource(R.string.save_button),
                onClick = viewModel::onSaveButtonClicked,
            )
        },
    ) { viewState ->
        GlobalVariableEditorContent(
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
            typeSpecificContentStartsWithTextField = when (variableType) {
                VariableType.CONSTANT,
                VariableType.SLIDER,
                VariableType.DATE,
                VariableType.TIME,
                VariableType.INCREMENT,
                VariableType.TIMESTAMP,
                -> true
                VariableType.TEXT,
                VariableType.NUMBER,
                VariableType.PASSWORD,
                VariableType.SELECT,
                VariableType.COLOR,
                VariableType.TOGGLE,
                VariableType.UUID,
                VariableType.CLIPBOARD,
                -> false
            },
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

    GlobalVariableEditorDialogs(
        dialogState = state?.dialogState,
        onDiscardDialogConfirmed = viewModel::onDiscardDialogConfirmed,
        onDismissed = viewModel::onDismissDialog,
    )
}
