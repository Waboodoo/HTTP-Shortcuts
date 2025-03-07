package ch.rmy.android.http_shortcuts.activities.editor.response

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.MultiSelectDialog
import ch.rmy.android.http_shortcuts.components.models.MenuEntry
import ch.rmy.android.http_shortcuts.data.enums.ResponseDisplayAction

@Composable
fun ResponseDisplayDialogs(
    dialogState: ResponseDisplayDialogState?,
    onActionsSelected: (List<ResponseDisplayAction>) -> Unit,
    onDismissed: () -> Unit,
) {
    when (dialogState) {
        is ResponseDisplayDialogState.SelectActions -> SelectActionsDialog(
            actions = dialogState.actions,
            onResult = onActionsSelected,
            onDismissed = onDismissed,
        )
        null -> Unit
    }
}

@Composable
private fun SelectActionsDialog(
    actions: Collection<ResponseDisplayAction>,
    onResult: (List<ResponseDisplayAction>) -> Unit,
    onDismissed: () -> Unit,
) {
    MultiSelectDialog(
        title = stringResource(R.string.title_select_response_toolbar_buttons),
        entries = listOf(
            MenuEntry(ResponseDisplayAction.RERUN, stringResource(R.string.action_rerun_shortcut)),
            MenuEntry(ResponseDisplayAction.SHARE, stringResource(R.string.share_button)),
            MenuEntry(ResponseDisplayAction.COPY, stringResource(R.string.action_copy_response)),
            MenuEntry(ResponseDisplayAction.SAVE, stringResource(R.string.button_save_response_as_file)),
        ),
        initiallyChecked = actions,
        allowEmpty = true,
        onDismissRequest = { selected ->
            if (selected != null) {
                onResult(selected)
            } else {
                onDismissed()
            }
        },
    )
}
