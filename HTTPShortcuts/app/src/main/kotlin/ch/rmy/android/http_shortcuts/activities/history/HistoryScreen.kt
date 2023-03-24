package ch.rmy.android.http_shortcuts.activities.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.rmy.android.framework.extensions.initialize
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.WithViewState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    topAppBarColors: TopAppBarColors,
    onEvent: (ViewModelEvent) -> Unit,
) {
    val viewModel = viewModel<HistoryViewModel>()
    val viewState by viewModel.viewState.collectAsState(initial = null)

    LaunchedEffect(Unit) {
        viewModel.initialize()
        viewModel.events.collect { event ->
            onEvent(event)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors,
                title = {
                    Text(stringResource(R.string.title_event_history))
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.onBackPressed()
                        },
                    ) {
                        Icon(Icons.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    if (viewState?.isClearButtonVisible == true) {
                        IconButton(
                            onClick = {
                                viewModel.onClearHistoryButtonClicked()
                            },
                        ) {
                            Icon(Icons.Filled.Delete, stringResource(R.string.button_clear_history))
                        }
                    }
                },
            )
        },
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .padding(contentPadding)
        ) {
            WithViewState(viewState) { state ->
                HistoryContent(
                    state,
                    onLongPressed = viewModel::onHistoryEventLongPressed,
                )
            }
        }
    }
}
