package ch.rmy.android.http_shortcuts.activities.variables.editor.types

import ch.rmy.android.http_shortcuts.data.domains.variables.TemporaryVariableRepository
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.variables.types.TimeType
import java.text.SimpleDateFormat
import java.util.Locale

class TimeTypeViewModel : BaseTypeViewModel() {

    override fun createViewState(variable: Variable) = TimeTypeViewState(
        timeFormat = TimeType.getTimeFormat(variable),
        rememberValue = variable.rememberValue,
    )

    override suspend fun save(temporaryVariableRepository: TemporaryVariableRepository, viewState: VariableTypeViewState) {
        viewState as TimeTypeViewState
        temporaryVariableRepository.setData(
            mapOf(TimeType.KEY_FORMAT to viewState.timeFormat),
        )
        temporaryVariableRepository.setRememberValue(viewState.rememberValue)
    }

    override fun validate(viewState: VariableTypeViewState): VariableTypeViewState? {
        viewState as TimeTypeViewState
        if (viewState.timeFormat.isEmpty()) {
            return viewState.copy(
                invalidFormat = true,
            )
        }
        try {
            SimpleDateFormat(viewState.timeFormat, Locale.US)
        } catch (e: IllegalArgumentException) {
            return viewState.copy(
                invalidFormat = true,
            )
        }
        return null
    }
}
