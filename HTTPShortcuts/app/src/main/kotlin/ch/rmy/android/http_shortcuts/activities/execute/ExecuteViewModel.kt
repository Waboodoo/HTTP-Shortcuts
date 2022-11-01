package ch.rmy.android.http_shortcuts.activities.execute

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.activities.execute.models.ExecutionParams
import ch.rmy.android.http_shortcuts.activities.execute.models.ExecutionStatus
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

class ExecuteViewModel(
    application: Application,
) : BaseViewModel<ExecutionParams, ExecuteViewState>(application), WithDialog {

    @Inject
    lateinit var executionFactory: ExecutionFactory

    init {
        getApplicationComponent().inject(this)
    }

    private lateinit var execution: Execution

    override var dialogState: DialogState?
        get() = currentViewState?.dialogState
        set(value) {
            updateViewState {
                copy(dialogState = value)
            }
        }

    override fun initViewState() = ExecuteViewState()

    override fun onInitializationStarted(data: ExecutionParams) {
        if (isRepetition()) {
            finalizeInitialization(silent = true)
            finish(skipAnimation = true)
            return
        }
        lastExecutionTime = SystemClock.elapsedRealtime()
        lastExecutionData = data

        viewModelScope.launch {
            execution = executionFactory.createExecution(data)
            finalizeInitialization(silent = true)
            execute()
        }
    }

    private fun isRepetition(): Boolean {
        val time = lastExecutionTime ?: return false
        val data = lastExecutionData ?: return false
        return data.executionId == null &&
            initData.executionId == null &&
            data.shortcutId == initData.shortcutId &&
            data.variableValues == initData.variableValues &&
            SystemClock.elapsedRealtime() - time < REPETITION_DEBOUNCE_TIME.inWholeMilliseconds
    }

    private fun execute() {
        viewModelScope.launch {
            try {
                execution.execute().collect { status ->
                    when (status) {
                        is ExecutionStatus.InProgress -> {
                            emitEvent(ExecuteEvent.ShowProgress)
                        }
                        is ExecutionStatus.WrappingUp -> {
                            emitEvent(ExecuteEvent.HideProgress)
                        }
                        else -> Unit
                    }
                }
            } finally {
                finish(skipAnimation = true)
            }
        }
    }

    companion object {
        private val REPETITION_DEBOUNCE_TIME = 500.milliseconds

        private var lastExecutionTime: Long? = null
        private var lastExecutionData: ExecutionParams? = null
    }
}
