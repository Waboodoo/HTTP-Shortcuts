package ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.SavedStateHandle
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.ConfirmDialog
import ch.rmy.android.http_shortcuts.components.IconPickerDialog
import ch.rmy.android.http_shortcuts.components.SelectDialog
import ch.rmy.android.http_shortcuts.components.SelectDialogEntry
import ch.rmy.android.http_shortcuts.components.ShortcutPickerDialog
import ch.rmy.android.http_shortcuts.components.VariablePickerDialog
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.dtos.ShortcutPlaceholder
import ch.rmy.android.http_shortcuts.extensions.localize
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

@Composable
fun CodeSnippetPickerDialogs(
    dialogState: CodeSnippetPickerDialogState?,
    savedStateHandle: SavedStateHandle,
    onShortcutSelected: (ShortcutId) -> Unit,
    onCurrentShortcutSelected: () -> Unit,
    onIconSelected: (ShortcutIcon) -> Unit,
    onCustomIconOptionSelected: () -> Unit,
    onVariableSelected: (VariableId) -> Unit,
    onVariableEditorButtonClicked: () -> Unit,
    onWorkingDirectorySelected: (String) -> Unit,
    onDismissRequested: () -> Unit,
) {
    when (dialogState) {
        is CodeSnippetPickerDialogState.SelectShortcut -> {
            SelectShortcutDialog(
                title = dialogState.title.localize(),
                shortcuts = dialogState.shortcuts,
                onShortcutSelected = onShortcutSelected,
                onCurrentShortcutSelected = onCurrentShortcutSelected,
                onDismissRequested = onDismissRequested,
            )
        }
        is CodeSnippetPickerDialogState.SelectIcon -> {
            SelectIcon(
                onCustomIconOptionSelected = onCustomIconOptionSelected,
                onIconSelected = onIconSelected,
                onDismissRequested = onDismissRequested,
            )
        }
        is CodeSnippetPickerDialogState.SelectVariableForReading -> {
            if (dialogState.variables.isEmpty()) {
                ConfirmDialog(
                    message = stringResource(R.string.help_text_code_snippet_get_variable_no_variable),
                    confirmButton = stringResource(R.string.button_create_first_variable),
                    onConfirmRequest = onVariableEditorButtonClicked,
                    onDismissRequest = onDismissRequested,
                )
            } else {
                VariablePickerDialog(
                    savedStateHandle = savedStateHandle,
                    title = stringResource(R.string.title_variables),
                    variables = dialogState.variables,
                    onVariableSelected = onVariableSelected,
                    onDismissRequested = onDismissRequested,
                )
            }
        }
        is CodeSnippetPickerDialogState.SelectVariableForWriting -> {
            if (dialogState.variables.isEmpty()) {
                ConfirmDialog(
                    message = stringResource(R.string.help_text_code_snippet_set_variable_no_variable),
                    confirmButton = stringResource(R.string.button_create_first_variable),
                    onConfirmRequest = onVariableEditorButtonClicked,
                    onDismissRequest = onDismissRequested,
                )
            } else {
                VariablePickerDialog(
                    savedStateHandle = savedStateHandle,
                    title = stringResource(R.string.title_variables),
                    variables = dialogState.variables,
                    onVariableSelected = onVariableSelected,
                    onDismissRequested = onDismissRequested,
                )
            }
        }
        is CodeSnippetPickerDialogState.SelectWorkingDirectory -> {
            SelectWorkingDirectory(
                directoryNames = dialogState.directoryNames,
                onWorkingDirectorySelected = onWorkingDirectorySelected,
                onDismissRequested = onDismissRequested,
            )
        }
        null -> Unit
    }
}

@Composable
private fun SelectShortcutDialog(
    title: String,
    shortcuts: List<ShortcutPlaceholder>,
    onShortcutSelected: (ShortcutId) -> Unit,
    onCurrentShortcutSelected: () -> Unit,
    onDismissRequested: () -> Unit,
) {
    ShortcutPickerDialog(
        title = title,
        shortcuts = shortcuts,
        includeCurrentShortcutOption = true,
        onShortcutSelected = onShortcutSelected,
        onCurrentShortcutSelected = onCurrentShortcutSelected,
        onDismissRequested = onDismissRequested,
    )
}

@Composable
private fun SelectIcon(
    onCustomIconOptionSelected: () -> Unit,
    onIconSelected: (ShortcutIcon) -> Unit,
    onDismissRequested: () -> Unit,
) {
    IconPickerDialog(
        title = stringResource(R.string.change_icon),
        onCustomIconOptionSelected = onCustomIconOptionSelected,
        onIconSelected = onIconSelected,
        onDismissRequested = onDismissRequested,
    )
}

@Composable
private fun SelectWorkingDirectory(
    directoryNames: List<String>,
    onWorkingDirectorySelected: (String) -> Unit,
    onDismissRequested: () -> Unit,
) {
    SelectDialog(
        title = stringResource(R.string.title_select_working_directory),
        onDismissRequest = onDismissRequested,
    ) {
        directoryNames.forEach { directoryName ->
            SelectDialogEntry(
                label = directoryName,
                onClick = {
                    onWorkingDirectorySelected(directoryName)
                },
            )
        }
    }
}
