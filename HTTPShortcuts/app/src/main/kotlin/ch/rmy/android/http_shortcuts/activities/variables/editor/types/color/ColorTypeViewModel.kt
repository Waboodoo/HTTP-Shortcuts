package ch.rmy.android.http_shortcuts.activities.variables.editor.types.color

import android.app.Application
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.BaseVariableTypeViewModel

class ColorTypeViewModel(application: Application) : BaseVariableTypeViewModel<Unit, ColorTypeViewState>(application) {

    override fun initViewState() = ColorTypeViewState(
        rememberValue = variable.rememberValue,
    )

    fun onRememberValueChanged(enabled: Boolean) {
        performOperation(
            temporaryVariableRepository.setRememberValue(enabled)
        )
    }
}
