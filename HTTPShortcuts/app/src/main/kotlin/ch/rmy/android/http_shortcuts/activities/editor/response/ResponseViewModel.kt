package ch.rmy.android.http_shortcuts.activities.editor.response

import android.app.Application
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.models.Shortcut

class ResponseViewModel(application: Application) : BaseViewModel<Unit, ResponseViewState>(application) {

    private val temporaryShortcutRepository = TemporaryShortcutRepository()
    private val variableRepository = VariableRepository()

    override fun initViewState() = ResponseViewState()

    override fun onInitialized() {
        temporaryShortcutRepository.getTemporaryShortcut()
            .subscribe(
                ::initViewStateFromShortcut,
                ::onInitializationError,
            )
            .attachTo(destroyer)

        variableRepository.getObservableVariables()
            .subscribe { variables ->
                updateViewState {
                    copy(variables = variables)
                }
            }
            .attachTo(destroyer)
    }

    private fun initViewStateFromShortcut(shortcut: Shortcut) {
        val responseHandling = shortcut.responseHandling!!
        updateViewState {
            copy(
                successMessageHint = getSuccessMessageHint(shortcut),
                responseUiType = responseHandling.uiType,
                responseSuccessOutput = responseHandling.successOutput,
                responseFailureOutput = responseHandling.failureOutput,
                includeMetaInformation = responseHandling.includeMetaInfo,
                successMessage = responseHandling.successMessage,
            )
        }
    }

    private fun getSuccessMessageHint(shortcut: Shortcut): Localizable =
        StringResLocalizable(
            R.string.executed,
            Localizable.create { context ->
                shortcut.name.ifEmpty { context.getString(R.string.shortcut_safe_name) }
            },
        )

    private fun onInitializationError(error: Throwable) {
        handleUnexpectedError(error)
        finish()
    }

    fun onResponseUiTypeChanged(responseUiType: String) {
        updateViewState {
            copy(responseUiType = responseUiType)
        }
        performOperation(
            temporaryShortcutRepository.setResponseUiType(responseUiType)
        )
    }

    fun onResponseSuccessOutputChanged(responseSuccessOutput: String) {
        updateViewState {
            copy(responseSuccessOutput = responseSuccessOutput)
        }
        performOperation(
            temporaryShortcutRepository.setResponseSuccessOutput(responseSuccessOutput)
        )
    }

    fun onResponseFailureOutputChanged(responseFailureOutput: String) {
        updateViewState {
            copy(responseFailureOutput = responseFailureOutput)
        }
        performOperation(
            temporaryShortcutRepository.setResponseFailureOutput(responseFailureOutput)
        )
    }

    fun onSuccessMessageChanged(successMessage: String) {
        updateViewState {
            copy(successMessage = successMessage)
        }
        performOperation(
            temporaryShortcutRepository.setResponseSuccessMessage(successMessage)
        )
    }

    fun onIncludeMetaInformationChanged(includeMetaInformation: Boolean) {
        updateViewState {
            copy(includeMetaInformation = includeMetaInformation)
        }
        performOperation(
            temporaryShortcutRepository.setResponseIncludeMetaInfo(includeMetaInformation)
        )
    }

    fun onBackPressed() {
        waitForOperationsToFinish {
            finish()
        }
    }
}
