package ch.rmy.android.http_shortcuts.variables.types

import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogState
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.models.Variable
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import javax.inject.Inject

class TimeType : BaseVariableType() {

    @Inject
    lateinit var variablesRepository: VariableRepository

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override suspend fun resolveValue(variable: Variable, dialogHandle: DialogHandle): String {
        val selectedTime = dialogHandle.showDialog(
            ExecuteDialogState.TimePicker(
                initialTime = getInitialTime(variable.value.takeIf { variable.rememberValue }),
            )
        )
        if (variable.rememberValue) {
            variablesRepository.setVariableValue(variable.id, TIME_FORMAT.format(selectedTime))
        }
        return DateTimeFormatter.ofPattern(getTimeFormat(variable), Locale.US)
            .format(selectedTime)
    }

    private fun getInitialTime(previousValue: String?): LocalTime =
        previousValue
            ?.let {
                try {
                    LocalTime.parse(it, TIME_FORMAT)
                } catch (e: DateTimeParseException) {
                    null
                }
            }
            ?: LocalTime.now()

    companion object {

        const val KEY_FORMAT = "format"
        private const val DEFAULT_FORMAT = "HH:mm"

        private val TIME_FORMAT = DateTimeFormatter.ofPattern("HH-mm", Locale.US)

        fun getTimeFormat(variable: Variable) =
            variable.dataForType[DateType.KEY_FORMAT] ?: DEFAULT_FORMAT
    }
}
