package ch.rmy.android.http_shortcuts.activities.variables.editor.types.text

import android.app.Application
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.BaseVariableTypeViewModel

class TextTypeViewModel(application: Application) : BaseVariableTypeViewModel<Unit, TextTypeViewState>(application) {

    override fun initViewState() = TextTypeViewState(
        rememberValue = variable.rememberValue,
        isMultiline = variable.isMultiline,
    )

    fun onRememberValueChanged(enabled: Boolean) {
        performOperation(
            temporaryVariableRepository.setRememberValue(enabled)
        )
    }

    fun onMultilineChanged(enabled: Boolean) {
        performOperation(
            temporaryVariableRepository.setMultiline(enabled)
        )
    }
}
