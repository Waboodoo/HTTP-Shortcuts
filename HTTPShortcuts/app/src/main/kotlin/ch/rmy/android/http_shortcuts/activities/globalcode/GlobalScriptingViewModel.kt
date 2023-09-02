package ch.rmy.android.http_shortcuts.activities.globalcode

import android.app.Application
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.app.AppRepository
import ch.rmy.android.http_shortcuts.scripting.CodeTransformer
import ch.rmy.android.http_shortcuts.utils.ExternalURLs
import kotlinx.coroutines.Dispatchers
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

    override suspend fun initialize(data: Unit): GlobalScriptingViewState {
        val globalCode = withContext(Dispatchers.Default) {
            codeTransformer.transformForEditing(appRepository.getGlobalCode())
        }
        previousGlobalCode = globalCode
        return GlobalScriptingViewState(
            globalCode = globalCode,
        )
    }

    fun onHelpButtonClicked() = runAction {
        openURL(ExternalURLs.SCRIPTING_DOCUMENTATION)
    }

    fun onBackPressed() = runAction {
        if (viewState.hasChanges) {
            updateDialogState(GlobalScriptingDialogState.DiscardWarning)
        } else {
            finish()
        }
    }

    fun onDiscardDialogConfirmed() = runAction {
        updateDialogState(null)
        finish()
    }

    fun onSaveButtonClicked() = runAction {
        appRepository.setGlobalCode(
            withContext(Dispatchers.Default) {
                viewState.globalCode
                    .trim()
                    .takeUnlessEmpty()
                    ?.let {
                        codeTransformer.transformForStoring(it)
                    }
            }
        )
    }

    fun onGlobalCodeChanged(globalCode: String) = runAction {
        updateViewState {
            copy(
                globalCode = globalCode,
                hasChanges = globalCode != previousGlobalCode,
            )
        }
    }

    fun onCodeSnippetPicked(textBeforeCursor: String, textAfterCursor: String) = runAction {
        emitEvent(
            GlobalScriptingEvent.InsertCodeSnippet(textBeforeCursor, textAfterCursor)
        )
    }

    fun onDialogDismissed() = runAction {
        updateDialogState(null)
    }

    private suspend fun updateDialogState(dialogState: GlobalScriptingDialogState?) {
        updateViewState {
            copy(dialogState = dialogState)
        }
    }
}
