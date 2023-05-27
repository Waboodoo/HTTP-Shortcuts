package ch.rmy.android.http_shortcuts.variables.types

import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogState
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.models.Variable
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import javax.inject.Inject

class DateType : BaseVariableType() {

    @Inject
    lateinit var variablesRepository: VariableRepository

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override suspend fun resolveValue(variable: Variable, dialogHandle: DialogHandle): String {
        val selectedDate = dialogHandle.showDialog(
            ExecuteDialogState.DatePicker(
                initialDate = getInitialDate(variable.value.takeIf { variable.rememberValue }),
            )
        )

        if (variable.rememberValue) {
            variablesRepository.setVariableValue(variable.id, DATE_FORMAT.format(selectedDate))
        }
        return DateTimeFormatter.ofPattern(getDateFormat(variable), Locale.US)
            .format(selectedDate)
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
            variable.dataForType[KEY_FORMAT] ?: DEFAULT_FORMAT
    }
}
