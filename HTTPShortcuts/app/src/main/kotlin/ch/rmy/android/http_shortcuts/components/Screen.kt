package ch.rmy.android.http_shortcuts.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.ViewModelEvent

@Composable
inline fun <D, VS, reified VM : BaseViewModel<D, VS>> bindViewModel(
    initData: D,
    key: String? = null,
): Pair<VM, VS?> {
    val viewModel = viewModel<VM>(key = key)
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val eventHandler = LocalEventinator.current
    var viewReady by remember {
        mutableStateOf(false)
    }
    LaunchedEffect(Unit) {
        viewModel.initialize(initData)
    }
    LaunchedEffect(viewReady) {
        if (viewReady) {
            viewModel.events.collect(eventHandler::onEvent)
        }
    }
    LaunchedEffect(state) {
        viewReady = state != null
    }
    return Pair(viewModel, state)
}

@Composable
inline fun <VS, reified VM : BaseViewModel<Unit, VS>> bindViewModel(): Pair<VM, VS?> =
    bindViewModel<Unit, VS, VM>(Unit)

@Composable
fun EventHandler(enabled: Boolean = true, onEvent: (ViewModelEvent) -> Boolean) {
    val eventHandler = LocalEventinator.current
    DisposableEffect(enabled) {
        if (enabled) {
            eventHandler.register(onEvent)
            onDispose {
                eventHandler.deregister(onEvent)
            }
        } else {
            onDispose { }
        }
    }
}

class Eventinator(val baseHandler: (ViewModelEvent) -> Unit = {}) {
    private val eventHandlers = mutableListOf<(ViewModelEvent) -> Boolean>()

    fun onEvent(event: ViewModelEvent) {
        for (handler in eventHandlers) {
            val handled = handler(event)
            if (handled) {
                return
            }
        }
        baseHandler(event)
    }

    fun register(eventHandler: (ViewModelEvent) -> Boolean) {
        eventHandlers.add(0, eventHandler)
    }

    fun deregister(eventHandler: (ViewModelEvent) -> Boolean) {
        eventHandlers.remove(eventHandler)
    }
}

val LocalEventinator = staticCompositionLocalOf {
    Eventinator()
}
