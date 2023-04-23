package ch.rmy.android.http_shortcuts.activities.editor.scripting

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
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
        finalizeInitialization(silent = true)
    }

    override fun initViewState() = ScriptingViewState()

    private var isFinishing: Boolean = false
    private var persistJob: Job? = null

    override fun onInitialized() {
        viewModelScope.launch {
            try {
                val temporaryShortcut = temporaryShortcutRepository.getTemporaryShortcut()
                initViewStateFromShortcut(temporaryShortcut)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                onInitializationError(e)
            }
        }
    }

    private fun initViewStateFromShortcut(shortcut: Shortcut) {
        viewModelScope.launch(Dispatchers.Default) {
            val codeOnPrepare = codeTransformer.transformForEditing(shortcut.codeOnPrepare)
            val codeOnSuccess = codeTransformer.transformForEditing(shortcut.codeOnSuccess)
            val codeOnFailure = codeTransformer.transformForEditing(shortcut.codeOnFailure)
            withContext(Dispatchers.Main) {
                updateViewState {
                    copy(
                        codeOnPrepare = codeOnPrepare,
                        codeOnSuccess = codeOnSuccess,
                        codeOnFailure = codeOnFailure,
                        shortcutExecutionType = shortcut.type,
                    )
                }
            }
        }
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
    }

    fun onCodeSuccessChanged(code: String) {
        updateViewState {
            copy(
                codeOnSuccess = code,
            )
        }
        schedulePersisting()
    }

    fun onCodeFailureChanged(code: String) {
        updateViewState {
            copy(
                codeOnFailure = code,
            )
        }
        schedulePersisting()
    }

    private fun schedulePersisting() {
        if (isFinishing) {
            return
        }
        persistJob?.cancel()
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
}
