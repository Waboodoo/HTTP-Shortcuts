package ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets

import android.content.ActivityNotFoundException
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.SavedStateHandle
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.EventHandler
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.ToolbarIcon
import ch.rmy.android.http_shortcuts.components.bindViewModel
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.logging.Logging.logException
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import ch.rmy.android.http_shortcuts.navigation.ResultHandler
import ch.rmy.android.http_shortcuts.plugin.TaskerTaskPickerContract
import ch.rmy.android.http_shortcuts.utils.RingtonePickerContract

@Composable
fun CodeSnippetPickerScreen(
    savedStateHandle: SavedStateHandle,
    currentShortcutId: ShortcutId?,
    includeSuccessOptions: Boolean,
    includeResponseOptions: Boolean,
    includeNetworkErrorOption: Boolean,
) {
    val (viewModel, state) = bindViewModel<CodeSnippetPickerViewModel.InitData, CodeSnippetPickerViewState, CodeSnippetPickerViewModel>(
        CodeSnippetPickerViewModel.InitData(
            currentShortcutId,
            includeSuccessOptions,
            includeResponseOptions,
            includeNetworkErrorOption,
        ),
    )

    val pickRingtone = rememberLauncherForActivityResult(RingtonePickerContract) { ringtone ->
        ringtone?.let(viewModel::onRingtoneSelected)
    }
    val pickTaskerTask = rememberLauncherForActivityResult(TaskerTaskPickerContract) { taskName ->
        taskName?.let(viewModel::onTaskerTaskSelected)
    }

    ResultHandler(savedStateHandle) { result ->
        when (result) {
            is NavigationDestination.IconPicker.Result -> {
                viewModel.onIconSelected(result.icon)
            }
        }
    }

    BackHandler(enabled = !state?.searchQuery.isNullOrEmpty()) {
        viewModel.onSearchQueryChanged("")
    }

    val context = LocalContext.current
    EventHandler { event ->
        when (event) {
            is CodeSnippetPickerEvent.OpenRingtonePicker -> consume {
                try {
                    pickRingtone.launch()
                } catch (e: ActivityNotFoundException) {
                    logException("CodeSnippetPicker", e)
                    context.showToast(R.string.error_generic)
                }
            }
            is CodeSnippetPickerEvent.OpenTaskerTaskPicker -> consume {
                try {
                    pickTaskerTask.launch()
                } catch (e: ActivityNotFoundException) {
                    logException("CodeSnippetPicker", e)
                    context.showToast(R.string.error_generic)
                }
            }
            else -> false
        }
    }

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.title_add_code_snippet),
        actions = {
            ToolbarIcon(
                Icons.AutoMirrored.Filled.HelpOutline,
                contentDescription = stringResource(R.string.button_show_help),
                onClick = viewModel::onHelpButtonClicked,
            )
        },
    ) { viewState ->
        CodeSnippetPickerContent(
            items = viewState.items,
            query = viewState.searchQuery,
            onQueryChanged = viewModel::onSearchQueryChanged,
            onSectionClicked = viewModel::onSectionClicked,
            onCodeSnippetItemClicked = viewModel::onCodeSnippetClicked,
            onDocumentationButtonClicked = viewModel::onCodeSnippetDocRefButtonClicked,
        )
    }

    CodeSnippetPickerDialogs(
        state?.dialogState,
        savedStateHandle = savedStateHandle,
        onShortcutSelected = viewModel::onShortcutSelected,
        onCurrentShortcutSelected = viewModel::onCurrentShortcutSelected,
        onIconSelected = viewModel::onIconSelected,
        onCustomIconOptionSelected = viewModel::onCustomIconOptionSelected,
        onVariableSelected = viewModel::onVariableSelected,
        onVariableEditorButtonClicked = viewModel::onVariableEditorButtonClicked,
        onWorkingDirectorySelected = viewModel::onWorkingDirectorySelected,
        onDismissRequested = viewModel::onDialogDismissRequested,
    )
}
