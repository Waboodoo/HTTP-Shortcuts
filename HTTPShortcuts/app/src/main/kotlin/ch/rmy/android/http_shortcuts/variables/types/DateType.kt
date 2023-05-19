package ch.rmy.android.http_shortcuts.variables.types

import android.app.DatePickerDialog
import android.content.DialogInterface
import ch.rmy.android.framework.extensions.showOrElse
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume

class DateType : BaseVariableType() {

    @Inject
    lateinit var variablesRepository: VariableRepository

    @Inject
    lateinit var activityProvider: ActivityProvider

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override suspend fun resolveValue(variable: Variable, dialogHandle: DialogHandle): String {
        val selectedDate = withContext(Dispatchers.Main) {
            suspendCancellableCoroutine<LocalDate> { continuation ->
                val date = getInitialDate(variable.value.takeIf { variable.rememberValue })
                val activity = activityProvider.getActivity()
                val datePicker = DatePickerDialog(
                    activity,
                    null,
                    date.year,
                    date.monthValue,
                    date.dayOfMonth,
                )
                if (variable.title.isNotEmpty()) {
                    datePicker.setTitle(variable.title)
                }
                datePicker.setButton(
                    DialogInterface.BUTTON_POSITIVE,
                    activity.getString(R.string.dialog_ok),
                ) { _, _ ->
                    val newDate = LocalDate.of(
                        datePicker.datePicker.year,
                        datePicker.datePicker.month,
                        datePicker.datePicker.dayOfMonth,
                    )
                    continuation.resume(newDate)
                }
                datePicker.setCancelable(true)
                datePicker.setCanceledOnTouchOutside(true)

                datePicker.showOrElse {
                    continuation.cancel()
                }
                datePicker.setOnDismissListener {
                    continuation.cancel()
                }
            }
        }

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

        internal val DATE_FORMAT = DateTimeFormatter.ofPattern(DEFAULT_FORMAT, Locale.US)

        fun getDateFormat(variable: Variable) =
            variable.dataForType[KEY_FORMAT] ?: DEFAULT_FORMAT
    }
}
