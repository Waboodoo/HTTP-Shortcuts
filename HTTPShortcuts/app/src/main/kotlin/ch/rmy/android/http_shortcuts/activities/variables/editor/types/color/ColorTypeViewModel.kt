package ch.rmy.android.http_shortcuts.activities.variables.editor.types.color

import android.app.Application
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.BaseVariableTypeViewModel
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent

class ColorTypeViewModel(application: Application) : BaseVariableTypeViewModel<Unit, ColorTypeViewState>(application) {

    init {
        getApplicationComponent().inject(this)
    }

    override fun initViewState() = ColorTypeViewState(
        rememberValue = variable.rememberValue,
    )

    fun onRememberValueChanged(enabled: Boolean) {
        launchWithProgressTracking {
            temporaryVariableRepository.setRememberValue(enabled)
        }
    }
}
