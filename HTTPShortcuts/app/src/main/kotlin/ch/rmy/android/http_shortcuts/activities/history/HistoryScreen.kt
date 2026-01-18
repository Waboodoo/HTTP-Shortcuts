package ch.rmy.android.http_shortcuts.activities.history

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
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
                painterResource(R.drawable.outline_nest_clock_farsight_analog_24),
                contentDescription = stringResource(R.string.button_history_toggle_time_mode),
                enabled = viewState.isTimeModeButtonEnabled,
                onClick = viewModel::onTimeModeToggleButtonClicked,
            )
            ToolbarIcon(
                painterResource(R.drawable.outline_delete_24),
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
