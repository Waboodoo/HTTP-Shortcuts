package ch.rmy.android.http_shortcuts.activities.variables.editor.types

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.activities.variables.editor.VariableTypeToVariableEditorEvent
import ch.rmy.android.http_shortcuts.data.domains.variables.TemporaryVariableRepository
import ch.rmy.android.http_shortcuts.data.models.VariableModel
import kotlinx.coroutines.launch
import javax.inject.Inject

abstract class BaseVariableTypeViewModel<InitData : Any, ViewState : Any>(application: Application) :
    BaseViewModel<InitData, ViewState>(application) {

    @Inject
    lateinit var temporaryVariableRepository: TemporaryVariableRepository

    protected lateinit var variable: VariableModel

    override fun onInitializationStarted(data: InitData) {
        viewModelScope.launch {
            temporaryVariableRepository.getObservableTemporaryVariable()
                .collect { variable ->
                    this@BaseVariableTypeViewModel.variable = variable
                    if (isInitialized) {
                        onVariableChanged()
                    } else {
                        finalizeInitialization()
                    }
                }
        }
    }

    protected open fun onVariableChanged() {
    }

    fun onValidationEvent() {
        viewModelScope.launch {
            waitForOperationsToFinish()
            sendValidationResult(validate())
        }
    }

    private fun sendValidationResult(valid: Boolean) {
        emitEvent(VariableTypeToVariableEditorEvent.Validated(valid))
    }

    protected open fun validate(): Boolean =
        true
}
