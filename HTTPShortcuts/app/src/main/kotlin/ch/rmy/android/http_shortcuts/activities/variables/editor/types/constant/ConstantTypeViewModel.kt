package ch.rmy.android.http_shortcuts.activities.variables.editor.types.constant

import android.app.Application
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.BaseVariableTypeViewModel
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId

class ConstantTypeViewModel(application: Application) :
    BaseVariableTypeViewModel<ConstantTypeViewModel.InitData, ConstantTypeViewState>(application) {

    init {
        getApplicationComponent().inject(this)
    }

    override fun initViewState() = ConstantTypeViewState(
        value = variable.value ?: "",
    )

    fun onValueChanged(value: String) {
        performOperation(
            temporaryVariableRepository.setValue(value)
        )
    }

    data class InitData(
        val variableId: VariableId? = null,
    )
}
