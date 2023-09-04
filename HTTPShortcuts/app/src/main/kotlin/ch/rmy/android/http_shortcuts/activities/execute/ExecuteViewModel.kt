package ch.rmy.android.http_shortcuts.activities.execute

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.ViewModelScope
import ch.rmy.android.http_shortcuts.activities.execute.models.ExecutionParams
import ch.rmy.android.http_shortcuts.activities.execute.models.ExecutionStatus
import ch.rmy.android.http_shortcuts.plugin.SessionMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class ExecuteViewModel
@Inject
constructor(
    application: Application,
    private val executionFactory: ExecutionFactory,
    private val sessionMonitor: SessionMonitor,
    private val dialogHandler: ExecuteDialogHandler,
) : BaseViewModel<ExecutionParams, ExecuteViewState>(application) {

    private lateinit var execution: Execution

    override suspend fun initialize(data: ExecutionParams): ExecuteViewState {
        if (isAccidentalRepetition()) {
            terminateInitialization()
        }

        sessionMonitor.onSessionStarted()
        lastExecutionTime = SystemClock.elapsedRealtime()
        lastExecutionData = data

        execution = executionFactory.createExecution(data, dialogHandler)

        viewModelScope.launch {
            dialogHandler.dialogState.collect { dialogState ->
                updateViewState {
                    copy(dialogState = dialogState)
                }
            }
        }
        runAction {
            execute()
        }
        return ExecuteViewState()
    }

    private fun isAccidentalRepetition(): Boolean {
        val time = lastExecutionTime ?: return false
        val data = lastExecutionData ?: return false
        return data.executionId == null &&
            initData.executionId == null &&
            data.shortcutId == initData.shortcutId &&
            data.variableValues == initData.variableValues &&
            SystemClock.elapsedRealtime() - time < ACCIDENTAL_REPETITION_DEBOUNCE_TIME.inWholeMilliseconds
    }

    private suspend fun ViewModelScope<ExecuteViewState>.execute() {
        var result: String? = null
        try {
            execution.execute().collect { status ->
                if (status is ExecutionStatus.WithResult) {
                    result = status.result
                }
                when (status) {
                    is ExecutionStatus.InProgress -> {
                        updateViewState {
                            copy(progressSpinnerVisible = true)
                        }
                    }
                    is ExecutionStatus.WrappingUp -> {
                        updateViewState {
                            copy(progressSpinnerVisible = false)
                        }
                    }
                    else -> Unit
                }
            }
        } finally {
            finish(skipAnimation = true)
            sessionMonitor.onSessionComplete(result)
        }
    }

    fun onDialogDismissed() {
        dialogHandler.onDialogDismissed()
    }

    fun onDialogResult(result: Any) {
        dialogHandler.onDialogResult(result)
    }

    companion object {
        private val ACCIDENTAL_REPETITION_DEBOUNCE_TIME = 500.milliseconds

        private var lastExecutionTime: Long? = null
        private var lastExecutionData: ExecutionParams? = null
    }
}
