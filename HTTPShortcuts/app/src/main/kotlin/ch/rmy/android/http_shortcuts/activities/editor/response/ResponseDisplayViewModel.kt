package ch.rmy.android.http_shortcuts.activities.editor.response

import android.app.Application
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.enums.ResponseContentType
import ch.rmy.android.http_shortcuts.data.enums.ResponseDisplayAction
import ch.rmy.android.http_shortcuts.data.enums.ResponseUiType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ResponseDisplayViewModel
@Inject
constructor(
    application: Application,
    private val temporaryShortcutRepository: TemporaryShortcutRepository,
) : BaseViewModel<Unit, ResponseDisplayViewState>(application) {

    override suspend fun initialize(data: Unit): ResponseDisplayViewState {
        val shortcut = temporaryShortcutRepository.getTemporaryShortcut()
        return ResponseDisplayViewState(
            responseUiType = shortcut.responseUiType,
            responseSuccessOutput = shortcut.responseSuccessOutput,
            responseContentType = shortcut.responseContentType,
            includeMetaInformation = shortcut.responseIncludeMetaInfo,
            responseDisplayActions = shortcut.responseDisplayActions,
            useMonospaceFont = shortcut.responseMonospace,
            fontSize = shortcut.responseFontSize,
            jsonArrayAsTable = shortcut.responseJsonArrayAsTable,
            javaScriptEnabled = shortcut.responseJavaScriptEnabled,
        )
    }

    fun onResponseContentTypeChanged(responseContentType: ResponseContentType?) = runAction {
        updateViewState {
            copy(responseContentType = responseContentType)
        }
        withProgressTracking {
            temporaryShortcutRepository.setResponseContentType(responseContentType)
        }
    }

    fun onIncludeMetaInformationChanged(includeMetaInformation: Boolean) = runAction {
        updateViewState {
            copy(includeMetaInformation = includeMetaInformation)
        }
        withProgressTracking {
            temporaryShortcutRepository.setResponseIncludeMetaInfo(includeMetaInformation)
        }
    }

    fun onWindowActionsButtonClicked() = runAction {
        if (viewState.responseUiType != ResponseUiType.WINDOW) {
            skipAction()
        }
        updateDialogState(
            ResponseDisplayDialogState.SelectActions(
                actions = viewState.responseDisplayActions,
            ),
        )
    }

    fun onDialogActionChanged(action: ResponseDisplayAction?) = runAction {
        if (viewState.responseUiType != ResponseUiType.DIALOG) {
            skipAction()
        }
        val actions = listOfNotNull(action)
        updateViewState {
            copy(responseDisplayActions = actions)
        }
        withProgressTracking {
            temporaryShortcutRepository.setDisplayActions(actions)
        }
    }

    fun onUseMonospaceFontChanged(monospace: Boolean) = runAction {
        updateViewState {
            copy(useMonospaceFont = monospace)
        }
        withProgressTracking {
            temporaryShortcutRepository.setUseMonospaceFont(monospace)
        }
    }

    fun onFontSizeChanged(fontSize: Int?) = runAction {
        updateViewState {
            copy(fontSize = fontSize)
        }
        withProgressTracking {
            temporaryShortcutRepository.setFontSize(fontSize)
        }
    }

    fun onWindowActionsSelected(responseDisplayActions: List<ResponseDisplayAction>) = runAction {
        val actions = listOf(
            ResponseDisplayAction.RERUN,
            ResponseDisplayAction.SHARE,
            ResponseDisplayAction.COPY,
            ResponseDisplayAction.SAVE,
        )
            .filter {
                it in responseDisplayActions
            }
        updateViewState {
            copy(
                dialogState = null,
                responseDisplayActions = actions,
            )
        }
        withProgressTracking {
            temporaryShortcutRepository.setDisplayActions(actions)
        }
    }

    fun onJsonArrayAsTableChanged(jsonArrayAsTable: Boolean) = runAction {
        updateViewState {
            copy(jsonArrayAsTable = jsonArrayAsTable)
        }
        withProgressTracking {
            temporaryShortcutRepository.setJsonArrayAsTable(jsonArrayAsTable)
        }
    }

    fun onJavaScriptEnabledChanged(enabled: Boolean) = runAction {
        updateViewState {
            copy(javaScriptEnabled = enabled)
        }
        withProgressTracking {
            temporaryShortcutRepository.setJavaScriptEnabled(enabled)
        }
    }

    fun onDismissDialog() = runAction {
        updateDialogState(null)
    }

    private suspend fun updateDialogState(dialogState: ResponseDisplayDialogState?) {
        updateViewState {
            copy(dialogState = dialogState)
        }
    }

    fun onBackPressed() = runAction {
        waitForOperationsToFinish()
        closeScreen()
    }
}
