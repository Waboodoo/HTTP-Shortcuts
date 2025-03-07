package ch.rmy.android.http_shortcuts.activities.history

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.ToolbarIcon
import ch.rmy.android.http_shortcuts.components.bindViewModel

@Composable
fun HistoryScreen() {
    val (viewModel, state) = bindViewModel<HistoryViewState, HistoryViewModel>()

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.title_event_history),
        actions = { viewState ->
            ToolbarIcon(
                Icons.Filled.AccessTime,
                contentDescription = stringResource(R.string.button_history_toggle_time_mode),
                enabled = viewState.isTimeModeButtonEnabled,
                onClick = viewModel::onTimeModeToggleButtonClicked,
            )
            ToolbarIcon(
                Icons.Filled.Delete,
                contentDescription = stringResource(R.string.button_clear_history),
                enabled = viewState.isClearButtonEnabled,
                onClick = viewModel::onClearHistoryButtonClicked,
            )
        },
    ) { viewState ->
        HistoryContent(
            viewState,
            onLongPressed = viewModel::onHistoryEventLongPressed,
        )
    }
}
