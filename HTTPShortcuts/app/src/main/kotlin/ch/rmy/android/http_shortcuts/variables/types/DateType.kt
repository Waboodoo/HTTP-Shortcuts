package ch.rmy.android.http_shortcuts.variables.types

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogState
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.models.Variable
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class DateType
@Inject
constructor(
    private val variablesRepository: VariableRepository,
) : VariableType {
    override suspend fun resolve(variable: Variable, dialogHandle: DialogHandle): String {
        val selectedDate = dialogHandle.showDialog(
            ExecuteDialogState.DatePicker(
                title = variable.title.takeUnlessEmpty(),
                initialDate = getInitialDate(variable.realValue.takeIf { variable.rememberValue }),
            ),
        )

        if (variable.rememberValue) {
            variablesRepository.setVariableValue(variable.id, DATE_FORMAT.format(selectedDate))
        }
        return SimpleDateFormat(getDateFormat(variable), Locale.US)
            .format(Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant()))
    }

    private fun getInitialDate(previousValue: String?): LocalDate =
        previousValue
            ?.let {
                try {
                    LocalDate.parse(it, DateTimeFormatter.ofPattern(DEFAULT_FORMAT, Locale.US))
                } catch (e: DateTimeParseException) {
                    null
                }
            }
            ?: LocalDate.now()

    companion object {

        const val KEY_FORMAT = "format"
        private const val DEFAULT_FORMAT = "yyyy-MM-dd"

        internal val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern(DEFAULT_FORMAT, Locale.US)

        fun getDateFormat(variable: Variable) =
            variable.getStringData(KEY_FORMAT) ?: DEFAULT_FORMAT
    }
}
