package ch.rmy.android.http_shortcuts.activities.variables.editor.types

import ch.rmy.android.http_shortcuts.data.domains.variables.TemporaryGlobalVariableRepository
import ch.rmy.android.http_shortcuts.data.models.GlobalVariable
import ch.rmy.android.http_shortcuts.variables.types.TimestampType
import java.text.SimpleDateFormat
import java.util.Locale

class TimestampTypeViewModel : BaseTypeViewModel() {

    override fun createViewState(variable: GlobalVariable) = TimestampTypeViewState(
        timeFormat = TimestampType.getTimeFormat(variable),
    )

    override suspend fun save(temporaryGlobalVariableRepository: TemporaryGlobalVariableRepository, viewState: VariableTypeViewState) {
        viewState as TimestampTypeViewState
        temporaryGlobalVariableRepository.setData(
            mapOf(TimestampType.KEY_FORMAT to viewState.timeFormat),
        )
    }

    override fun validate(viewState: VariableTypeViewState): VariableTypeViewState? {
        viewState as TimestampTypeViewState
        if (viewState.timeFormat.isEmpty()) {
            return viewState.copy(
                invalidFormat = true,
            )
        }
        try {
            SimpleDateFormat(viewState.timeFormat, Locale.getDefault())
        } catch (e: IllegalArgumentException) {
            return viewState.copy(
                invalidFormat = true,
            )
        }
        return null
    }
}
