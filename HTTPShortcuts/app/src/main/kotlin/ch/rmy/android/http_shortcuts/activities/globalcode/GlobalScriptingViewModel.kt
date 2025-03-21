package ch.rmy.android.http_shortcuts.activities.globalcode

import android.app.Application
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.data.domains.app_config.AppConfigRepository
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import ch.rmy.android.http_shortcuts.scripting.CodeTransformer
import ch.rmy.android.http_shortcuts.utils.ExternalURLs
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltViewModel
class GlobalScriptingViewModel
@Inject
constructor(
    application: Application,
    private val appConfigRepository: AppConfigRepository,
    private val codeTransformer: CodeTransformer,
) : BaseViewModel<Unit, GlobalScriptingViewState>(application) {
    private var previousGlobalCode = ""

    override suspend fun initialize(data: Unit): GlobalScriptingViewState {
        val globalCode = withContext(Dispatchers.Default) {
            codeTransformer.transformForEditing(appConfigRepository.getGlobalCode())
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
            closeScreen()
        }
    }

    fun onDiscardDialogConfirmed() = runAction {
        updateDialogState(null)
        closeScreen()
    }

    fun onSaveButtonClicked() = runAction {
        appConfigRepository.setGlobalCode(
            withContext(Dispatchers.Default) {
                viewState.globalCode
                    .trim()
                    .let {
                        codeTransformer.transformForStoring(it)
                    }
            },
        )
        closeScreen()
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
            GlobalScriptingEvent.InsertCodeSnippet(textBeforeCursor, textAfterCursor),
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

    fun onCodeSnippetButtonClicked() = runAction {
        navigate(
            NavigationDestination.CodeSnippetPicker.buildRequest(),
        )
    }
}
