package ch.rmy.android.http_shortcuts.activities.editor.scripting

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.activities.editor.scripting.models.CodeFieldType
import ch.rmy.android.http_shortcuts.activities.execute.ExecutionStarter
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import ch.rmy.android.http_shortcuts.scripting.CodeTransformer
import ch.rmy.android.http_shortcuts.utils.ExternalURLs
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.LinkedList
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class ScriptingViewModel
@Inject
constructor(
    application: Application,
    private val temporaryShortcutRepository: TemporaryShortcutRepository,
    private val codeTransformer: CodeTransformer,
    private val executionStarter: ExecutionStarter,
) : BaseViewModel<ScriptingViewModel.InitData, ScriptingViewState>(application) {

    private lateinit var shortcutExecutionType: ShortcutExecutionType

    private var isFinishing: Boolean = false
    private var persistJob: Job? = null
        set(value) {
            field?.cancel()
            field = value
        }

    private val history = LinkedList<HistoryState>()
    private var captureHistoryStateJob: Job? = null
        set(value) {
            field?.cancel()
            field = value
        }

    override suspend fun initialize(data: InitData): ScriptingViewState {
        val shortcut = temporaryShortcutRepository.getTemporaryShortcut()
        val historyState = withContext(Dispatchers.Default) {
            HistoryState(
                codeOnPrepare = codeTransformer.transformForEditing(shortcut.codeOnPrepare),
                codeOnSuccess = codeTransformer.transformForEditing(shortcut.codeOnSuccess),
                codeOnFailure = codeTransformer.transformForEditing(shortcut.codeOnFailure),
            )
        }
        history.push(historyState)
        shortcutExecutionType = shortcut.executionType
        return ScriptingViewState(
            codeOnPrepare = historyState.codeOnPrepare,
            codeOnSuccess = historyState.codeOnSuccess,
            codeOnFailure = historyState.codeOnFailure,
            shortcutExecutionType = shortcutExecutionType,
        )
    }

    fun onCodePrepareChanged(code: String) = runAction {
        updateViewState {
            copy(
                codeOnPrepare = code,
            )
        }
        schedulePersisting()
        scheduleHistoryCapture()
    }

    fun onCodeSuccessChanged(code: String) = runAction {
        updateViewState {
            copy(
                codeOnSuccess = code,
            )
        }
        schedulePersisting()
        scheduleHistoryCapture()
    }

    fun onCodeFailureChanged(code: String) = runAction {
        updateViewState {
            copy(
                codeOnFailure = code,
            )
        }
        schedulePersisting()
        scheduleHistoryCapture()
    }

    private fun schedulePersisting() {
        if (isFinishing) {
            return
        }
        persistJob = viewModelScope.launch(Dispatchers.Default) {
            delay(500.milliseconds)
            with(getCurrentViewState()) {
                temporaryShortcutRepository.setCode(
                    codeTransformer.transformForStoring(codeOnPrepare),
                    codeTransformer.transformForStoring(codeOnSuccess),
                    codeTransformer.transformForStoring(codeOnFailure),
                )
            }
        }
    }

    private fun scheduleHistoryCapture() {
        if (isFinishing) {
            captureHistoryStateJob = null
            return
        }
        captureHistoryStateJob = viewModelScope.launch {
            delay(500.milliseconds)
            val viewState = getCurrentViewState()
            val newState = HistoryState(viewState.codeOnPrepare, viewState.codeOnSuccess, viewState.codeOnFailure)
            if (history.peekLast() == newState) {
                return@launch
            }
            history.add(newState)
            while (history.size > MAX_HISTORY_SIZE) {
                history.removeFirst()
            }
            updateViewState {
                copy(isUndoButtonEnabled = true)
            }
        }
    }

    fun onHelpButtonClicked() = runAction {
        openURL(ExternalURLs.SCRIPTING_DOCUMENTATION)
    }

    fun onBackPressed() = runAction {
        if (isFinishing) {
            skipAction()
        }
        isFinishing = true
        persistJob?.join()
        closeScreen()
    }

    fun onCodeSnippetPicked(textBeforeCursor: String, textAfterCursor: String) = runAction {
        emitEvent(
            ScriptingEvent.InsertCodeSnippet(
                textBeforeCursor = textBeforeCursor,
                textAfterCursor = textAfterCursor,
            ),
        )
    }

    fun onTestButtonClicked() = runAction {
        waitForOperationsToFinish()
        executionStarter.execute(
            shortcutId = Shortcut.TEMPORARY_ID,
            trigger = ShortcutTriggerType.TEST_IN_EDITOR,
        )
    }

    fun onUndoButtonClicked() = runAction {
        history.pollLast()
            ?: skipAction()
        val historyState = history.peekLast()
            ?: skipAction()
        updateViewState {
            copy(
                codeOnPrepare = historyState.codeOnPrepare,
                codeOnFailure = historyState.codeOnFailure,
                codeOnSuccess = historyState.codeOnSuccess,
            )
        }
        schedulePersisting()

        if (history.size <= 1) {
            updateViewState {
                copy(isUndoButtonEnabled = false)
            }
        }
    }

    fun onCodeSnippetButtonClicked(activeField: CodeFieldType) = runAction {
        navigate(
            when (activeField) {
                CodeFieldType.PREPARE -> NavigationDestination.CodeSnippetPicker.buildRequest(
                    shortcutId = initData.currentShortcutId,
                )
                CodeFieldType.SUCCESS -> NavigationDestination.CodeSnippetPicker.buildRequest(
                    shortcutId = initData.currentShortcutId,
                    includeSuccessOptions = true,
                    includeResponseOptions = true,
                )
                CodeFieldType.FAILURE -> NavigationDestination.CodeSnippetPicker.buildRequest(
                    shortcutId = initData.currentShortcutId,
                    includeResponseOptions = true,
                    includeNetworkErrorOption = true,
                )
            },
        )
    }

    data class HistoryState(
        val codeOnPrepare: String,
        var codeOnSuccess: String,
        var codeOnFailure: String,
    )

    data class InitData(
        val currentShortcutId: ShortcutId?,
    )

    companion object {
        private const val MAX_HISTORY_SIZE = 30
    }
}
