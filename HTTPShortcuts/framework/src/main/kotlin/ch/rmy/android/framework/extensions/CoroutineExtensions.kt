package ch.rmy.android.framework.extensions

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

fun <ViewState : Any> LifecycleOwner.collectViewStateWhileActive(viewModel: BaseViewModel<*, ViewState>, onViewStateUpdate: (ViewState) -> Unit) {
    whileLifecycleActive {
        viewModel.viewState.collectLatest(onViewStateUpdate)
    }
}

fun LifecycleOwner.collectEventsWhileActive(viewModel: BaseViewModel<*, *>, onEvent: (ViewModelEvent) -> Unit) {
    whileLifecycleActive {
        viewModel.events.collect(onEvent)
    }
}

fun LifecycleOwner.whileLifecycleActive(block: suspend CoroutineScope.() -> Unit) {
    lifecycleScope.launch {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED, block)
    }
}

fun Continuation<Unit>.resume() {
    resume(Unit)
}
