package ch.rmy.android.http_shortcuts.activities.variables.editor.types.text

import android.app.Application
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.BaseVariableTypeViewModel
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.enums.VariableType

class TextTypeViewModel(application: Application) : BaseVariableTypeViewModel<Unit, TextTypeViewState>(application) {

    init {
        getApplicationComponent().inject(this)
    }

    override fun initViewState() = TextTypeViewState(
        rememberValue = variable.rememberValue,
        isMultiline = variable.isMultiline,
        isMultilineCheckboxVisible = variable.variableType == VariableType.TEXT,
    )

    fun onRememberValueChanged(enabled: Boolean) {
        launchWithProgressTracking {
            temporaryVariableRepository.setRememberValue(enabled)
        }
    }

    fun onMultilineChanged(enabled: Boolean) {
        launchWithProgressTracking {
            temporaryVariableRepository.setMultiline(enabled)
        }
    }
}
