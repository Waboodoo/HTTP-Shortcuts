package ch.rmy.android.http_shortcuts.activities.editor.response

import android.app.Application
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.editor.basicsettings.BasicRequestSettingsEvent
import ch.rmy.android.http_shortcuts.activities.variables.VariablesActivity
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.usecases.GetVariablePlaceholderPickerDialogUseCase
import ch.rmy.android.http_shortcuts.usecases.KeepVariablePlaceholderProviderUpdatedUseCase
import javax.inject.Inject

class ResponseViewModel(application: Application) : BaseViewModel<Unit, ResponseViewState>(application), WithDialog {

    @Inject
    lateinit var temporaryShortcutRepository: TemporaryShortcutRepository

    @Inject
    lateinit var keepVariablePlaceholderProviderUpdated: KeepVariablePlaceholderProviderUpdatedUseCase

    @Inject
    lateinit var getVariablePlaceholderPickerDialog: GetVariablePlaceholderPickerDialogUseCase

    init {
        getApplicationComponent().inject(this)
    }

    override var dialogState: DialogState?
        get() = currentViewState?.dialogState
        set(value) {
            updateViewState {
                copy(dialogState = value)
            }
        }

    override fun onInitializationStarted(data: Unit) {
        finalizeInitialization(silent = true)
    }

    override fun initViewState() = ResponseViewState()

    override fun onInitialized() {
        temporaryShortcutRepository.getTemporaryShortcut()
            .subscribe(
                ::initViewStateFromShortcut,
                ::onInitializationError,
            )
            .attachTo(destroyer)

        keepVariablePlaceholderProviderUpdated(::emitCurrentViewState)
            .attachTo(destroyer)
    }

    private fun initViewStateFromShortcut(shortcut: ShortcutModel) {
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

    private fun getSuccessMessageHint(shortcut: ShortcutModel): Localizable =
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

    fun onSuccessMessageVariableButtonClicked() {
        dialogState = getVariablePlaceholderPickerDialog.invoke(
            onVariableSelected = {
                emitEvent(ResponseEvent.InsertVariablePlaceholder(it))
            },
            onEditVariableButtonClicked = {
                openActivity(
                    VariablesActivity.IntentBuilder()
                )
            },
        )
    }

    fun onBackPressed() {
        waitForOperationsToFinish {
            finish()
        }
    }
}
