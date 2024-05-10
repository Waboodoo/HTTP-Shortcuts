package ch.rmy.android.framework.viewmodel

import kotlinx.coroutines.CoroutineScope

interface ViewModelScope<ViewState> : CoroutineScope {

    val viewState: ViewState

    suspend fun withProgressTracking(operation: suspend () -> Unit)

    suspend fun updateViewState(mutation: ViewState.() -> ViewState)

    fun skipAction(): Nothing {
        throw ViewModelCancellationException()
    }
}
