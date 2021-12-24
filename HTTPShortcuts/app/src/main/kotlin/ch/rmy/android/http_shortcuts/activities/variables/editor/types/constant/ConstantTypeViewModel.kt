package ch.rmy.android.http_shortcuts.activities.variables.editor.types.constant

import android.app.Application
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.BaseVariableTypeViewModel
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository

class ConstantTypeViewModel(application: Application) :
    BaseVariableTypeViewModel<ConstantTypeViewModel.InitData, ConstantTypeViewState>(application) {

    private val variableRepository = VariableRepository()

    override fun initViewState() = ConstantTypeViewState(
        value = variable.value ?: "",
        variables = null,
    )

    override fun onInitialized() {
        super.onInitialized()
        variableRepository.getObservableVariables()
            .subscribe { variables ->
                updateViewState {
                    copy(variables = variables.filterNot { it.id == initData.variableId })
                }
            }
            .attachTo(destroyer)
    }

    fun onValueChanged(value: String) {
        performOperation(
            temporaryVariableRepository.setValue(value)
        )
    }

    data class InitData(
        val variableId: String? = null,
    )
}
