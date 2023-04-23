package ch.rmy.android.http_shortcuts.activities.globalcode

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.app.AppRepository
import ch.rmy.android.http_shortcuts.scripting.CodeTransformer
import ch.rmy.android.http_shortcuts.utils.ExternalURLs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GlobalScriptingViewModel(application: Application) : BaseViewModel<Unit, GlobalScriptingViewState>(application) {

    @Inject
    lateinit var appRepository: AppRepository

    @Inject
    lateinit var codeTransformer: CodeTransformer

    init {
        getApplicationComponent().inject(this)
    }

    private var previousGlobalCode = ""

    override fun initViewState() = GlobalScriptingViewState()

    override fun onInitializationStarted(data: Unit) {
        viewModelScope.launch(Dispatchers.Default) {
            val globalCode = codeTransformer.transformForEditing(appRepository.getGlobalCode())
            previousGlobalCode = globalCode
            updateViewState {
                copy(globalCode = globalCode)
            }
            withContext(Dispatchers.Main) {
                finalizeInitialization()
            }
        }
    }

    fun onHelpButtonClicked() {
        openURL(ExternalURLs.SCRIPTING_DOCUMENTATION)
    }

    fun onBackPressed() {
        updateDialogState(GlobalScriptingDialogState.DiscardWarning)
    }

    fun onDiscardDialogConfirmed() {
        updateDialogState(null)
        finish()
    }

    fun onSaveButtonClicked() {
        val viewState = currentViewState ?: return
        viewModelScope.launch(Dispatchers.Default) {
            appRepository.setGlobalCode(
                viewState.globalCode
                    .trim()
                    .takeUnlessEmpty()
                    ?.let {
                        codeTransformer.transformForStoring(it)
                    }
            )
            finish()
        }
    }

    fun onGlobalCodeChanged(globalCode: String) {
        updateViewState {
            copy(
                globalCode = globalCode,
                hasChanges = globalCode != previousGlobalCode,
            )
        }
    }

    fun onCodeSnippetPicked(textBeforeCursor: String, textAfterCursor: String) {
        emitEvent(
            GlobalScriptingEvent.InsertCodeSnippet(textBeforeCursor, textAfterCursor)
        )
    }

    fun onDialogDismissed() {
        updateDialogState(null)
    }

    private fun updateDialogState(dialogState: GlobalScriptingDialogState?) {
        updateViewState {
            copy(dialogState = dialogState)
        }
    }
}
