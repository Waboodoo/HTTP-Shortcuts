package ch.rmy.android.http_shortcuts.activities.editor.scripting

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.type
import ch.rmy.android.http_shortcuts.scripting.CodeTransformer
import ch.rmy.android.http_shortcuts.utils.ExternalURLs
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.LinkedList
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

class ScriptingViewModel(application: Application) : BaseViewModel<Unit, ScriptingViewState>(application) {

    @Inject
    lateinit var temporaryShortcutRepository: TemporaryShortcutRepository

    @Inject
    lateinit var codeTransformer: CodeTransformer

    init {
        getApplicationComponent().inject(this)
    }

    override fun onInitializationStarted(data: Unit) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val temporaryShortcut = temporaryShortcutRepository.getTemporaryShortcut()
                initViewStateFromShortcut(temporaryShortcut)
                withContext(Dispatchers.Main) {
                    finalizeInitialization()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onInitializationError(e)
                }
            }
        }
    }

    private lateinit var shortcutExecutionType: ShortcutExecutionType

    private suspend fun initViewStateFromShortcut(shortcut: Shortcut) {
        history.push(
            HistoryState(
                codeOnPrepare = codeTransformer.transformForEditing(shortcut.codeOnPrepare),
                codeOnSuccess = codeTransformer.transformForEditing(shortcut.codeOnSuccess),
                codeOnFailure = codeTransformer.transformForEditing(shortcut.codeOnFailure),
            ),
        )
        shortcutExecutionType = shortcut.type
    }

    override fun initViewState(): ScriptingViewState {
        val historyState = history.peekLast()!!
        return ScriptingViewState(
            codeOnPrepare = historyState.codeOnPrepare,
            codeOnSuccess = historyState.codeOnSuccess,
            codeOnFailure = historyState.codeOnFailure,
            shortcutExecutionType = shortcutExecutionType,
        )
    }

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

    private fun onInitializationError(error: Throwable) {
        handleUnexpectedError(error)
        finish()
    }

    fun onCodePrepareChanged(code: String) {
        updateViewState {
            copy(
                codeOnPrepare = code,
            )
        }
        schedulePersisting()
        scheduleHistoryCapture()
    }

    fun onCodeSuccessChanged(code: String) {
        updateViewState {
            copy(
                codeOnSuccess = code,
            )
        }
        schedulePersisting()
        scheduleHistoryCapture()
    }

    fun onCodeFailureChanged(code: String) {
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
        persistJob = viewModelScope.launch(Dispatchers.IO) {
            delay(500.milliseconds)
            currentViewState?.run {
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
            val viewState = currentViewState ?: return@launch
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

    fun onHelpButtonClicked() {
        openURL(ExternalURLs.SCRIPTING_DOCUMENTATION)
    }

    fun onBackPressed() {
        if (isFinishing) {
            return
        }
        isFinishing = true
        viewModelScope.launch {
            persistJob?.join()
            finish()
        }
    }

    fun onCodeSnippetPicked(textBeforeCursor: String, textAfterCursor: String) {
        emitEvent(
            ScriptingEvent.InsertCodeSnippet(
                textBeforeCursor = textBeforeCursor,
                textAfterCursor = textAfterCursor,
            )
        )
    }

    fun onTestButtonClicked() {
        viewModelScope.launch {
            waitForOperationsToFinish()
            openActivity(ExecuteActivity.IntentBuilder(Shortcut.TEMPORARY_ID).trigger(ShortcutTriggerType.TEST_IN_EDITOR))
        }
    }

    fun onUndoButtonClicked() {
        history.pollLast()
            ?: return
        val historyState = history.peekLast()
            ?: return
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

    data class HistoryState(
        val codeOnPrepare: String,
        var codeOnSuccess: String,
        var codeOnFailure: String,
    )

    companion object {
        private const val MAX_HISTORY_SIZE = 30
    }
}
