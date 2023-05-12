package ch.rmy.android.framework.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.annotation.UiThread
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.R
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.runFor
import ch.rmy.android.framework.ui.IntentBuilder
import ch.rmy.android.framework.utils.localization.Localizable
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

abstract class BaseViewModel<InitData : Any, ViewState : Any>(application: Application) : AndroidViewModel(application) {

    protected lateinit var initData: InitData
        private set

    private val eventChannel = Channel<ViewModelEvent>(
        capacity = Channel.UNLIMITED,
    )

    val events: Flow<ViewModelEvent> = eventChannel.receiveAsFlow()

    private val mutableViewState = MutableStateFlow<ViewState?>(null)
    val viewState: StateFlow<ViewState?> = mutableViewState.asStateFlow()

    protected val currentViewState: ViewState?
        get() = mutableViewState.value

    private val inProgress = AtomicInteger()
    private var nothingInProgress: CompletableDeferred<Unit>? = null

    protected fun emitEvent(event: ViewModelEvent) {
        eventChannel.trySend(event)
    }

    private val delayedViewStateUpdates = mutableListOf<ViewState.() -> ViewState>()
    private val delayedViewStateActions = mutableListOf<(ViewState) -> Unit>()

    @UiThread
    protected fun doWithViewState(action: (ViewState) -> Unit) {
        if (currentViewState != null) {
            action(currentViewState!!)
        } else {
            delayedViewStateActions.add(action)
        }
    }

    @UiThread
    protected fun updateViewState(mutation: ViewState.() -> ViewState) {
        if (currentViewState == null) {
            delayedViewStateUpdates.add(mutation)
            return
        }
        mutableViewState.update { it!!.mutation() }
    }

    private var isInitializationStarted = false

    protected var isInitialized: Boolean = false
        private set

    fun initialize(data: InitData) {
        if (isInitializationStarted) {
            if (data != initData) {
                logException(IllegalStateException("cannot re-initialize view model with different data"))
            }
            return
        }
        this.initData = data
        isInitializationStarted = true
        onInitializationStarted(data)
    }

    /**
     * Must eventually call finalizeInitialization or terminate the view
     */
    protected open fun onInitializationStarted(data: InitData) {
        finalizeInitialization()
    }

    protected fun finalizeInitialization(silent: Boolean = false) {
        if (isInitialized) {
            error("view model already initialized")
        }
        val currentViewState = initViewState()
            .runFor(delayedViewStateUpdates) {
                it()
            }
        delayedViewStateUpdates.clear()
        mutableViewState.value = currentViewState
        isInitialized = true
        onInitialized()
        delayedViewStateActions.forEach { it(currentViewState) }
        delayedViewStateActions.clear()
    }

    protected open fun onInitialized() {
    }

    protected abstract fun initViewState(): ViewState

    protected fun launchWithProgressTracking(operation: suspend () -> Unit) =
        viewModelScope.launch {
            operation()
        }

    protected suspend fun withProgressTracking(operation: suspend () -> Unit) {
        if (inProgress.incrementAndGet() == 1) {
            nothingInProgress = CompletableDeferred()
        }
        try {
            operation()
        } finally {
            if (inProgress.decrementAndGet() == 0) {
                nothingInProgress?.complete(Unit)
                nothingInProgress = null
            }
        }
    }

    protected fun handleUnexpectedError(error: Throwable) {
        logException(error)
        showSnackbar(R.string.error_generic, long = true)
    }

    protected suspend fun waitForOperationsToFinish() {
        nothingInProgress?.await()
    }

    protected fun showSnackbar(@StringRes stringRes: Int, long: Boolean = false) {
        emitEvent(ViewModelEvent.ShowSnackbar(stringRes, long = long))
    }

    protected fun showSnackbar(message: Localizable, long: Boolean = false) {
        emitEvent(ViewModelEvent.ShowSnackbar(message, long = long))
    }

    protected fun showToast(@StringRes stringRes: Int, long: Boolean = false) {
        emitEvent(ViewModelEvent.ShowToast(stringRes, long = long))
    }

    protected open fun finish(result: Int? = null, intent: Intent? = null, skipAnimation: Boolean = false) {
        emitEvent(ViewModelEvent.Finish(result, intent, skipAnimation))
    }

    protected fun finishWithOkResult(intent: Intent? = null) {
        finish(
            result = Activity.RESULT_OK,
            intent = intent,
        )
    }

    protected fun setResult(result: Int = Activity.RESULT_CANCELED, intent: Intent? = null) {
        emitEvent(ViewModelEvent.SetResult(result, intent))
    }

    protected fun openURL(url: String) {
        emitEvent(ViewModelEvent.OpenURL(url))
    }

    protected fun openURL(url: Uri) {
        emitEvent(ViewModelEvent.OpenURL(url.toString()))
    }

    protected fun openActivity(intentBuilder: IntentBuilder) {
        emitEvent(ViewModelEvent.OpenActivity(intentBuilder))
    }

    protected fun openActivity(intent: Intent) {
        emitEvent(
            ViewModelEvent.OpenActivity(object : IntentBuilder {
                override fun build(context: Context): Intent =
                    intent
            })
        )
    }

    protected fun sendBroadcast(intent: Intent) {
        emitEvent(ViewModelEvent.SendBroadcast(intent))
    }
}
