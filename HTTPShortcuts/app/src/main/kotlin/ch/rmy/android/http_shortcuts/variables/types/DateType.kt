package ch.rmy.android.http_shortcuts.variables.types

import android.app.DatePickerDialog
import android.content.DialogInterface
import ch.rmy.android.framework.extensions.applyIfNotNull
import ch.rmy.android.framework.extensions.showOrElse
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.models.VariableModel
import ch.rmy.android.http_shortcuts.extensions.parseOrNull
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
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

    override suspend fun resolveValue(variable: VariableModel): String {
        val selectedDate = withContext(Dispatchers.Main) {
            suspendCancellableCoroutine<Date> { continuation ->
                val calendar = getInitialDate(variable.value.takeIf { variable.rememberValue })
                val activity = activityProvider.getActivity()
                val datePicker = DatePickerDialog(
                    activity,
                    null,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH),
                )
                if (variable.title.isNotEmpty()) {
                    datePicker.setTitle(variable.title)
                }
                datePicker.setButton(
                    DialogInterface.BUTTON_POSITIVE,
                    activity.getString(R.string.dialog_ok),
                ) { _, _ ->
                    val newDate = Calendar.getInstance()
                    val day = datePicker.datePicker.dayOfMonth
                    val month = datePicker.datePicker.month
                    val year = datePicker.datePicker.year
                    newDate.set(year, month, day)
                    continuation.resume(newDate.time)
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
            variablesRepository.setVariableValue(variable.id, DATE_FORMAT.format(selectedDate.time))
        }
        return SimpleDateFormat(getDateFormat(variable), Locale.US)
            .format(selectedDate.time)
    }

    private fun getInitialDate(previousValue: String?) =
        Calendar.getInstance()
            .applyIfNotNull(previousValue) {
                time = DATE_FORMAT.parseOrNull(it) ?: return@applyIfNotNull
            }

    companion object {

        const val KEY_FORMAT = "format"
        private const val DEFAULT_FORMAT = "yyyy-MM-dd"

        internal val DATE_FORMAT
            get() = SimpleDateFormat(DEFAULT_FORMAT, Locale.US)

        fun getDateFormat(variable: VariableModel) =
            variable.dataForType[KEY_FORMAT] ?: DEFAULT_FORMAT
    }
}
