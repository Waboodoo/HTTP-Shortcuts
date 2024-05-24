package ch.rmy.android.http_shortcuts.activities.editor.response

import android.app.Application
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.enums.ResponseContentType
import ch.rmy.android.http_shortcuts.data.enums.ResponseDisplayAction
import ch.rmy.android.http_shortcuts.data.models.ResponseHandling
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.charset.Charset
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
        val responseHandling = shortcut.responseHandling!!

        runAction {
            val charsets = withContext(Dispatchers.Default) {
                Charset.availableCharsets().values.toList()
            }
            updateViewState {
                copy(availableCharsets = charsets)
            }
        }

        return ResponseDisplayViewState(
            responseUiType = responseHandling.uiType,
            responseSuccessOutput = responseHandling.successOutput,
            responseContentType = responseHandling.responseContentType,
            responseCharset = responseHandling.charsetOverride,
            availableCharsets = emptyList(),
            includeMetaInformation = responseHandling.includeMetaInfo,
            responseDisplayActions = responseHandling.displayActions,
            useMonospaceFont = responseHandling.monospace,
            fontSize = responseHandling.fontSize,
            jsonArrayAsTable = responseHandling.jsonArrayAsTable,
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

    fun onResponseCharsetChanged(charset: Charset?) = runAction {
        updateViewState {
            copy(responseCharset = charset)
        }
        withProgressTracking {
            temporaryShortcutRepository.setCharsetOverride(charset)
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
        if (viewState.responseUiType != ResponseHandling.UI_TYPE_WINDOW) {
            skipAction()
        }
        updateDialogState(
            ResponseDisplayDialogState.SelectActions(
                actions = viewState.responseDisplayActions,
            ),
        )
    }

    fun onDialogActionChanged(action: ResponseDisplayAction?) = runAction {
        if (viewState.responseUiType != ResponseHandling.UI_TYPE_DIALOG) {
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
