package ch.rmy.android.http_shortcuts.activities.execute

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.ViewModelScope
import ch.rmy.android.http_shortcuts.activities.execute.models.ExecutionParams
import ch.rmy.android.http_shortcuts.activities.execute.models.ExecutionStatus
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.extensions.shouldUseForegroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.launch

@HiltViewModel
class ExecuteViewModel
@Inject
constructor(
    application: Application,
    private val executionFactory: ExecutionFactory,
    private val dialogHandler: ExecuteDialogHandler,
    private val shortcutRepository: ShortcutRepository,
) : BaseViewModel<ExecutionParams, ExecuteViewState>(application) {

    private lateinit var execution: Execution

    override suspend fun initialize(data: ExecutionParams): ExecuteViewState {
        if (isAccidentalRepetition()) {
            terminateInitialization()
        }

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
            if (shortcutRepository.shouldUseForegroundService(data.shortcutId)) {
                emitEvent(StartServiceEvent)
                finish(skipAnimation = true)
            } else {
                execute()
            }
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
        try {
            execution.execute().collect { status ->
                when (status) {
                    is ExecutionStatus.InProgress -> {
                        updateViewState {
                            copy(executionInProgress = true)
                        }
                    }
                    is ExecutionStatus.WrappingUp -> {
                        updateViewState {
                            copy(executionInProgress = false)
                        }
                    }
                    else -> Unit
                }
            }
        } finally {
            finish(skipAnimation = true)
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
