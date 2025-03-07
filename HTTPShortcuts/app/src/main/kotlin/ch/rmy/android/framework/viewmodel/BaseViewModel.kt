package ch.rmy.android.framework.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.awaitNonNull
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.navigation.NavigationRequest
import ch.rmy.android.framework.ui.IntentBuilder
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.http_shortcuts.R
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class BaseViewModel<InitData : Any, ViewState : Any>(application: Application) : AndroidViewModel(application) {

    protected lateinit var initData: InitData
        private set

    private val eventChannel = Channel<ViewModelEvent>(
        capacity = Channel.UNLIMITED,
    )
    val events: Flow<ViewModelEvent> = eventChannel.receiveAsFlow()

    private val mutableViewState = MutableStateFlow<ViewState?>(null)
    val viewStateFlow: StateFlow<ViewState?> = mutableViewState.asStateFlow()

    private val inProgress = MutableStateFlow(0)

    protected suspend fun emitEvent(event: ViewModelEvent) {
        eventChannel.send(event)
    }

    protected suspend fun getCurrentViewState(): ViewState =
        mutableViewState.value
            ?: mutableViewState.awaitNonNull()

    protected suspend fun updateViewState(mutation: ViewState.() -> ViewState) {
        mutableViewState.awaitNonNull()
        mutableViewState.update { it!!.mutation() }
    }

    private var isInitializationStarted = false

    fun init(data: InitData) {
        synchronized(this) {
            if (isInitializationStarted) {
                if (data != initData) {
                    logInfo("Previous init data = $initData")
                    logInfo("New init data = $data")
                    logException(IllegalStateException("cannot re-initialize view model with different data"))
                }
                viewModelScope.launch {
                    onReactivated()
                }
                return
            }
            this.initData = data
            isInitializationStarted = true
        }

        viewModelScope.launch {
            try {
                mutableViewState.value = initialize(data)
            } catch (e: ViewModelCancellationException) {
                closeScreen()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                closeScreen()
                handleUnexpectedError(e)
            }
        }
    }

    protected abstract suspend fun initialize(data: InitData): ViewState

    protected open suspend fun onReactivated() {}

    protected fun terminateInitialization(): Nothing {
        throw ViewModelCancellationException()
    }

    protected fun runAction(action: suspend ViewModelScope<ViewState>.() -> Unit) {
        viewModelScope.launch {
            val scope = createViewModelScope()
            try {
                with(scope) {
                    action()
                }
            } catch (e: ViewModelCancellationException) {
                logInfo("Action skipped")
            }
        }
    }

    private suspend fun createViewModelScope(): ViewModelScope<ViewState> {
        val currentViewState = getCurrentViewState()
        return object : ViewModelScope<ViewState> {
            override val coroutineContext: CoroutineContext
                get() = viewModelScope.coroutineContext

            override val viewState: ViewState
                get() = currentViewState

            override suspend fun withProgressTracking(operation: suspend () -> Unit) {
                inProgress.update { it + 1 }
                try {
                    operation()
                } finally {
                    inProgress.update { it - 1 }
                }
            }

            override suspend fun updateViewState(mutation: ViewState.() -> ViewState) {
                mutableViewState.update { it!!.mutation() }
            }
        }
    }

    protected suspend fun waitForOperationsToFinish() {
        inProgress.first { it == 0 }
    }

    protected suspend fun handleUnexpectedError(error: Throwable) {
        logException(error)
        showSnackbar(R.string.error_generic, long = true)
    }

    protected suspend fun showSnackbar(@StringRes stringRes: Int, long: Boolean = false) {
        emitEvent(ViewModelEvent.ShowSnackbar(stringRes, long = long))
    }

    protected suspend fun showSnackbar(message: Localizable, long: Boolean = false) {
        emitEvent(ViewModelEvent.ShowSnackbar(message, long = long))
    }

    protected suspend fun showToast(@StringRes stringRes: Int, long: Boolean = false) {
        emitEvent(ViewModelEvent.ShowToast(stringRes, long = long))
    }

    protected suspend fun closeScreen(result: Any? = null) {
        emitEvent(ViewModelEvent.CloseScreen(result = result))
    }

    protected suspend fun finish(intent: Intent? = null, skipAnimation: Boolean = false, okResultCode: Boolean = false) {
        emitEvent(
            ViewModelEvent.Finish(
                resultCode = if (okResultCode) Activity.RESULT_OK else null,
                skipAnimation = skipAnimation,
                intent = intent,
            ),
        )
    }

    protected suspend fun setActivityResult(result: Int = Activity.RESULT_CANCELED, intent: Intent? = null) {
        emitEvent(ViewModelEvent.SetActivityResult(result, intent))
    }

    protected suspend fun openURL(url: String) {
        emitEvent(ViewModelEvent.OpenURL(url))
    }

    protected suspend fun openURL(url: Uri) {
        emitEvent(ViewModelEvent.OpenURL(url.toString()))
    }

    protected suspend fun navigate(navigationRequest: NavigationRequest) {
        emitEvent(ViewModelEvent.Navigate(navigationRequest))
    }

    protected suspend fun sendIntent(intentBuilder: IntentBuilder) {
        emitEvent(ViewModelEvent.SendIntent(intentBuilder))
    }

    protected suspend fun sendIntent(intent: Intent) {
        emitEvent(
            ViewModelEvent.SendIntent(object : IntentBuilder {
                override fun build(context: Context): Intent =
                    intent
            }),
        )
    }

    protected suspend fun sendBroadcast(intent: Intent) {
        emitEvent(ViewModelEvent.SendBroadcast(intent))
    }
}
