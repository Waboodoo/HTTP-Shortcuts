package ch.rmy.android.http_shortcuts.activities.execute

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.activities.execute.models.ExecutionParams
import ch.rmy.android.http_shortcuts.activities.execute.models.ExecutionStatus
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.plugin.SessionMonitor
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

class ExecuteViewModel(
    application: Application,
) : BaseViewModel<ExecutionParams, ExecuteViewState>(application) {

    @Inject
    lateinit var executionFactory: ExecutionFactory

    @Inject
    lateinit var sessionMonitor: SessionMonitor

    init {
        getApplicationComponent().inject(this)
    }

    private lateinit var execution: Execution

    override fun initViewState() = ExecuteViewState()

    override fun onInitializationStarted(data: ExecutionParams) {
        if (isAccidentalRepetition()) {
            finish(skipAnimation = true)
            return
        }
        sessionMonitor.onSessionStarted()
        lastExecutionTime = SystemClock.elapsedRealtime()
        lastExecutionData = data

        viewModelScope.launch {
            execution = executionFactory.createExecution(data)
            finalizeInitialization(silent = true)
            execute()
        }
    }

    override fun onInitialized() {
        viewModelScope.launch {
            execution.dialogState.collect { dialogState ->
                updateViewState {
                    copy(dialogState = dialogState)
                }
            }
        }
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

    private fun execute() {
        viewModelScope.launch {
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
    }

    fun onDialogDismissed() {
        execution.onDialogDismissed()
    }

    fun onDialogResult(result: Any) {
        execution.onDialogResult(result)
    }

    companion object {
        private val ACCIDENTAL_REPETITION_DEBOUNCE_TIME = 500.milliseconds

        private var lastExecutionTime: Long? = null
        private var lastExecutionData: ExecutionParams? = null
    }
}
