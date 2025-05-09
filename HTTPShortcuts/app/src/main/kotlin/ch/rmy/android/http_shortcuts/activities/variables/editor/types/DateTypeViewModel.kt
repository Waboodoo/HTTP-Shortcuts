package ch.rmy.android.http_shortcuts.activities.variables.editor.types

import ch.rmy.android.http_shortcuts.data.domains.variables.TemporaryGlobalVariableRepository
import ch.rmy.android.http_shortcuts.data.models.GlobalVariable
import ch.rmy.android.http_shortcuts.variables.types.DateType
import java.text.SimpleDateFormat
import java.util.Locale

class DateTypeViewModel : BaseTypeViewModel() {

    override fun createViewState(variable: GlobalVariable) = DateTypeViewState(
        dateFormat = DateType.getDateFormat(variable),
        rememberValue = variable.rememberValue,
    )

    override suspend fun save(temporaryGlobalVariableRepository: TemporaryGlobalVariableRepository, viewState: VariableTypeViewState) {
        viewState as DateTypeViewState
        temporaryGlobalVariableRepository.setData(
            mapOf(DateType.KEY_FORMAT to viewState.dateFormat),
        )
        temporaryGlobalVariableRepository.setRememberValue(viewState.rememberValue)
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
