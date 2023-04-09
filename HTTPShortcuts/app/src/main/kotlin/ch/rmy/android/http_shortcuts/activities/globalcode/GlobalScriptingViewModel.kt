package ch.rmy.android.http_shortcuts.activities.globalcode

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.app.AppRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.extensions.createDialogState
import ch.rmy.android.http_shortcuts.utils.ExternalURLs
import kotlinx.coroutines.launch
import javax.inject.Inject

class GlobalScriptingViewModel(application: Application) : BaseViewModel<Unit, GlobalScriptingViewState>(application), WithDialog {

    @Inject
    lateinit var appRepository: AppRepository

    @Inject
    lateinit var shortcutRepository: ShortcutRepository

    @Inject
    lateinit var variableRepository: VariableRepository

    init {
        getApplicationComponent().inject(this)
    }

    private var shortcutsInitialized = false
    private var variablesInitialized = false
    private var globalCodeInitialized = false
    private var previousGlobalCode = ""

    override var dialogState: DialogState?
        get() = currentViewState?.dialogState
        set(value) {
            updateViewState {
                copy(dialogState = value)
            }
        }

    override fun initViewState() = GlobalScriptingViewState()

    override fun onInitialized() {
        viewModelScope.launch {
            shortcutRepository.getObservableShortcuts()
                .collect { shortcuts ->
                    shortcutsInitialized = true
                    updateViewState {
                        copy(shortcuts = shortcuts)
                    }
                }
        }

        viewModelScope.launch {
            variableRepository.getObservableVariables()
                .collect { variables ->
                    variablesInitialized = true
                    initializeGlobalCodeIfPossible()
                    updateViewState {
                        copy(variables = variables)
                    }
                }
        }
    }

    private fun initializeGlobalCodeIfPossible() {
        if (globalCodeInitialized || !shortcutsInitialized || !variablesInitialized) {
            return
        }
        globalCodeInitialized = true
        viewModelScope.launch {
            val globalCode = appRepository.getGlobalCode()
            previousGlobalCode = globalCode
            updateViewState {
                copy(globalCode = globalCode)
            }
        }
    }

    fun onHelpButtonClicked() {
        openURL(ExternalURLs.SCRIPTING_DOCUMENTATION)
    }

    fun onBackPressed() {
        doWithViewState { viewState ->
            if (viewState.saveButtonVisible) {
                dialogState = createDialogState {
                    message(R.string.confirm_discard_changes_message)
                        .positive(R.string.dialog_discard) { onDiscardDialogConfirmed() }
                        .negative(R.string.dialog_cancel)
                        .build()
                }
            } else {
                finish()
            }
        }
    }

    private fun onDiscardDialogConfirmed() {
        finish()
    }

    fun onSaveButtonClicked() {
        doWithViewState { viewState ->
            viewModelScope.launch {
                appRepository.setGlobalCode(
                    viewState.globalCode
                        .trim()
                        .takeUnlessEmpty()
                )
                finish()
            }
        }
    }

    fun onGlobalCodeChanged(globalCode: String) {
        if (!globalCodeInitialized) {
            return
        }
        updateViewState {
            copy(
                globalCode = globalCode,
                saveButtonVisible = globalCode != previousGlobalCode,
            )
        }
    }

    fun onCodeSnippetButtonClicked() {
        emitEvent(GlobalScriptingEvent.ShowCodeSnippetPicker)
    }

    fun onCodeSnippetPicked(textBeforeCursor: String, textAfterCursor: String) {
        emitEvent(
            GlobalScriptingEvent.InsertCodeSnippet(textBeforeCursor, textAfterCursor)
        )
    }
}
