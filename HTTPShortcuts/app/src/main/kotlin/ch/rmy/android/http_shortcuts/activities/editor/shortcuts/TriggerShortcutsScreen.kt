package ch.rmy.android.http_shortcuts.activities.editor.shortcuts

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.FloatingAddButton
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.bindViewModel
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId

@Composable
fun TriggerShortcutsScreen(
    currentShortcutId: ShortcutId?,
) {
    val (viewModel, state) = bindViewModel<TriggerShortcutsViewModel.InitData, TriggerShortcutsViewState, TriggerShortcutsViewModel>(
        TriggerShortcutsViewModel.InitData(currentShortcutId),
    )

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.label_trigger_shortcuts),
        floatingActionButton = {
            FloatingAddButton(
                onClick = viewModel::onAddButtonClicked,
                contentDescription = stringResource(R.string.accessibility_label_add_shortcuts_to_trigger_fab),
            )
        },
    ) { viewState ->
        TriggerShortcutsContent(
            shortcuts = viewState.shortcuts,
            onShortcutClicked = viewModel::onShortcutClicked,
            onShortcutMoved = viewModel::onShortcutMoved,
        )
    }

    TriggerShortcutsDialogs(
        dialogState = state?.dialogState,
        onShortcutAddConfirmed = viewModel::onAddShortcutDialogConfirmed,
        onShortcutRemoveConfirmed = viewModel::onRemoveShortcutDialogConfirmed,
        onDismissed = viewModel::onDismissDialog,
    )
}
