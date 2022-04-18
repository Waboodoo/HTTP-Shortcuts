package ch.rmy.android.http_shortcuts.activities.settings.globalcode

import android.app.Application
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.domains.app.AppRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.usecases.GetBuiltInIconPickerDialogUseCase
import ch.rmy.android.http_shortcuts.usecases.GetIconPickerDialogUseCase
import ch.rmy.android.http_shortcuts.utils.ExternalURLs
import io.reactivex.android.schedulers.AndroidSchedulers

class GlobalScriptingViewModel(application: Application) : BaseViewModel<Unit, GlobalScriptingViewState>(application), WithDialog {

    private val appRepository = AppRepository()
    private val shortcutRepository = ShortcutRepository()
    private val variableRepository = VariableRepository()
    private val getIconPickerDialog = GetIconPickerDialogUseCase()
    private val getBuiltInIconPickerDialog = GetBuiltInIconPickerDialogUseCase()

    private var shortcutsInitialized = false
    private var variablesInitialized = false
    private var globalCodeInitialized = false

    var iconPickerShortcutPlaceholder: String? = null

    override var dialogState: DialogState?
        get() = currentViewState?.dialogState
        set(value) {
            updateViewState {
                copy(dialogState = value)
            }
        }

    override fun initViewState() = GlobalScriptingViewState()

    override fun onInitialized() {
        shortcutRepository.getObservableShortcuts()
            .subscribe { shortcuts ->
                shortcutsInitialized = true
                updateViewState {
                    copy(shortcuts = shortcuts)
                }
            }
            .attachTo(destroyer)

        variableRepository.getObservableVariables()
            .subscribe { variables ->
                variablesInitialized = true
                initializeGlobalCodeIfPossible()
                updateViewState {
                    copy(variables = variables)
                }
            }
            .attachTo(destroyer)
    }

    private fun initializeGlobalCodeIfPossible() {
        if (globalCodeInitialized || !shortcutsInitialized || !variablesInitialized) {
            return
        }
        globalCodeInitialized = true
        appRepository.getGlobalCode()
            .subscribe { globalCode ->
                updateViewState {
                    copy(globalCode = globalCode)
                }
            }
            .attachTo(destroyer)
    }

    fun onHelpButtonClicked() {
        openURL(ExternalURLs.SCRIPTING_DOCUMENTATION)
    }

    fun onBackPressed() {
        doWithViewState { viewState ->
            if (viewState.saveButtonVisible) {
                dialogState = DialogState.create {
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
            appRepository.setGlobalCode(viewState.globalCode.trim().takeUnlessEmpty())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    finish()
                }
                .attachTo(destroyer)
        }
    }

    fun onGlobalCodeChanged(globalCode: String) {
        if (!globalCodeInitialized) {
            return
        }
        updateViewState {
            copy(
                globalCode = globalCode,
                saveButtonVisible = true,
            )
        }
    }

    fun onChangeIconOptionSelected(shortcutPlaceholder: String) {
        iconPickerShortcutPlaceholder = shortcutPlaceholder
        dialogState = getIconPickerDialog(
            callbacks = object : GetIconPickerDialogUseCase.Callbacks {
                override fun openBuiltInIconSelectionDialog() {
                    dialogState = getBuiltInIconPickerDialog(::onIconSelected)
                }

                override fun openCustomIconPicker() {
                    emitEvent(GlobalScriptingEvent.OpenCustomIconPicker)
                }

                override fun openIpackPicker() {
                    emitEvent(GlobalScriptingEvent.OpenIpackIconPicker)
                }
            },
        )
    }

    fun onIconSelected(icon: ShortcutIcon) {
        emitEvent(GlobalScriptingEvent.InsertChangeIconSnippet(iconPickerShortcutPlaceholder ?: return, icon))
        iconPickerShortcutPlaceholder = null
    }

    fun onCodeSnippetButtonClicked() {
        emitEvent(GlobalScriptingEvent.ShowCodeSnippetPicker)
    }
}
