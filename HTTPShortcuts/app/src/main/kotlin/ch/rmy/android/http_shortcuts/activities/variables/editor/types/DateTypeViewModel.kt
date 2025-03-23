package ch.rmy.android.http_shortcuts.activities.variables.editor.types

import ch.rmy.android.http_shortcuts.data.domains.variables.TemporaryVariableRepository
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.variables.types.DateType
import java.text.SimpleDateFormat
import java.util.Locale

class DateTypeViewModel : BaseTypeViewModel() {

    override fun createViewState(variable: Variable) = DateTypeViewState(
        dateFormat = DateType.getDateFormat(variable),
        rememberValue = variable.rememberValue,
    )

    override suspend fun save(temporaryVariableRepository: TemporaryVariableRepository, viewState: VariableTypeViewState) {
        viewState as DateTypeViewState
        temporaryVariableRepository.setData(
            mapOf(DateType.KEY_FORMAT to viewState.dateFormat),
        )
        temporaryVariableRepository.setRememberValue(viewState.rememberValue)
    }

    override fun validate(viewState: VariableTypeViewState): VariableTypeViewState? {
        viewState as DateTypeViewState
        if (viewState.dateFormat.isEmpty()) {
            return viewState.copy(
                invalidFormat = true,
            )
        }
        try {
            SimpleDateFormat(viewState.dateFormat, Locale.US)
        } catch (e: IllegalArgumentException) {
            return viewState.copy(
                invalidFormat = true,
            )
        }
        return null
    }
}
