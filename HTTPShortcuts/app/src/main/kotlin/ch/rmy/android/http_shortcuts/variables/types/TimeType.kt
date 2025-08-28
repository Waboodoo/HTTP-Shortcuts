package ch.rmy.android.http_shortcuts.variables.types

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogState
import ch.rmy.android.http_shortcuts.data.domains.variables.GlobalVariableRepository
import ch.rmy.android.http_shortcuts.data.models.GlobalVariable
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class TimeType
@Inject
constructor(
    private val variablesRepository: GlobalVariableRepository,
) : VariableType {
    override suspend fun resolve(variable: GlobalVariable, dialogHandle: DialogHandle): String {
        val selectedTime = dialogHandle.showDialog(
            ExecuteDialogState.TimePicker(
                title = variable.title.takeUnlessEmpty(),
                initialTime = getInitialTime(variable.value.takeIf { variable.rememberValue }),
            ),
        )
        if (variable.rememberValue) {
            variablesRepository.setVariableValue(variable.id, TIME_FORMAT.format(selectedTime))
        }
        return SimpleDateFormat(getTimeFormat(variable), Locale.getDefault())
            .format(Date.from(LocalDate.now().atTime(selectedTime).atZone(ZoneOffset.systemDefault()).toInstant()))
    }

    private fun getInitialTime(previousValue: String?): LocalTime =
        previousValue
            ?.let {
                try {
                    LocalTime.parse(it, TIME_FORMAT)
                } catch (_: DateTimeParseException) {
                    null
                }
            }
            ?: LocalTime.now()

    companion object {

        const val KEY_FORMAT = "format"
        private const val DEFAULT_FORMAT = "HH:mm"

        private val TIME_FORMAT = DateTimeFormatter.ofPattern("HH-mm", Locale.US)

        fun getTimeFormat(variable: GlobalVariable) =
            variable.getStringData(DateType.KEY_FORMAT) ?: DEFAULT_FORMAT
    }
}
