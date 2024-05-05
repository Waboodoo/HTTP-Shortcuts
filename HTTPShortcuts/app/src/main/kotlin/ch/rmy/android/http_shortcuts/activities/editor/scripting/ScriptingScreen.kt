package ch.rmy.android.http_shortcuts.activities.editor.scripting

import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.PostAdd
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.SavedStateHandle
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.editor.scripting.models.CodeFieldType
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.ToolbarIcon
import ch.rmy.android.http_shortcuts.components.bindViewModel
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import ch.rmy.android.http_shortcuts.navigation.ResultHandler

@Composable
fun ScriptingScreen(
    savedStateHandle: SavedStateHandle,
    currentShortcutId: ShortcutId?,
) {
    val (viewModel, state) = bindViewModel<ScriptingViewModel.InitData, ScriptingViewState, ScriptingViewModel>(
        ScriptingViewModel.InitData(
            currentShortcutId = currentShortcutId,
        ),
    )

    var activeField by rememberSaveable(key = "active_field") {
        mutableStateOf(CodeFieldType.PREPARE)
    }

    ResultHandler(savedStateHandle) { result ->
        if (result is NavigationDestination.CodeSnippetPicker.Result) {
            viewModel.onCodeSnippetPicked(result.textBeforeCursor, result.textAfterCursor)
        }
    }

    BackHandler(state != null) {
        viewModel.onBackPressed()
    }

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.label_scripting),
        actions = { viewState ->
            ToolbarIcon(
                Icons.AutoMirrored.Filled.Undo,
                enabled = viewState.isUndoButtonEnabled,
                contentDescription = stringResource(R.string.button_undo),
                onClick = viewModel::onUndoButtonClicked,
            )
            if (viewState.isTestButtonVisible) {
                ToolbarIcon(
                    Icons.Filled.PlayArrow,
                    enabled = viewState.isTestButtonEnabled,
                    contentDescription = stringResource(R.string.test_button),
                    onClick = viewModel::onTestButtonClicked,
                )
            }
            ToolbarIcon(
                Icons.AutoMirrored.Filled.HelpOutline,
                contentDescription = stringResource(R.string.button_show_help),
                onClick = viewModel::onHelpButtonClicked,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.onCodeSnippetButtonClicked(activeField)
                },
            ) {
                Icon(
                    imageVector = Icons.Outlined.PostAdd,
                    contentDescription = stringResource(R.string.button_add_code_snippet),
                )
            }
        },
    ) { viewState ->
        ScriptingContent(
            activeFieldType = activeField,
            codeOnPrepare = viewState.codeOnPrepare,
            codeOnSuccess = viewState.codeOnSuccess,
            codeOnFailure = viewState.codeOnFailure,
            shortcutExecutionType = viewState.shortcutExecutionType,
            onActiveFieldChanged = {
                activeField = it
            },
            onCodeOnPrepareChanged = viewModel::onCodePrepareChanged,
            onCodeOnSuccessChanged = viewModel::onCodeSuccessChanged,
            onCodeOnFailureChanged = viewModel::onCodeFailureChanged,
        )
    }
}
