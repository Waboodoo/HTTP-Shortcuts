package ch.rmy.android.http_shortcuts.activities.editor.scripting

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.outlined.PostAdd
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets.CodeSnippetPickerActivity
import ch.rmy.android.http_shortcuts.activities.editor.scripting.models.CodeFieldType
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.ToolbarIcon
import ch.rmy.android.http_shortcuts.components.bindViewModel
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId

@Composable
fun ScriptingScreen(currentShortcutId: ShortcutId?) {
    val (viewModel, state) = bindViewModel<ScriptingViewState, ScriptingViewModel>()

    var activeField by rememberSaveable(key = "active_field") {
        mutableStateOf(CodeFieldType.PREPARE)
    }

    val pickCodeSnippet = rememberLauncherForActivityResult(CodeSnippetPickerActivity.PickCodeSnippet) { result ->
        if (result != null) {
            viewModel.onCodeSnippetPicked(result.textBeforeCursor, result.textAfterCursor)
        }
    }

    BackHandler {
        viewModel.onBackPressed()
    }

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.label_scripting),
        actions = {
            ToolbarIcon(
                Icons.Filled.HelpOutline,
                contentDescription = stringResource(R.string.button_show_help),
                onClick = viewModel::onHelpButtonClicked,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    pickCodeSnippet.launch {
                        when (activeField) {
                            CodeFieldType.PREPARE -> {
                                includeResponseOptions(false)
                                    .includeNetworkErrorOption(false)
                            }
                            CodeFieldType.SUCCESS -> {
                                includeResponseOptions(true)
                                    .includeNetworkErrorOption(false)
                            }
                            CodeFieldType.FAILURE -> {
                                includeResponseOptions(true)
                                    .includeNetworkErrorOption(true)
                            }
                        }
                            .runIfNotNull(currentShortcutId) {
                                currentShortcutId(it)
                            }
                    }
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
