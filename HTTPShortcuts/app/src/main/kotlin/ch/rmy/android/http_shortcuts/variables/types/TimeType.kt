package ch.rmy.android.http_shortcuts.variables.types

import android.app.TimePickerDialog
import android.text.format.DateFormat
import ch.rmy.android.framework.extensions.applyIfNotNull
import ch.rmy.android.framework.extensions.showOrElse
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

class TimeType : BaseVariableType() {

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
                val calendar = getInitialTime(variable.value.takeIf { variable.rememberValue })
                val activity = activityProvider.getActivity()
                val timePicker = TimePickerDialog(
                    activity,
                    { _, hourOfDay, minute ->
                        val newDate = Calendar.getInstance()
                        newDate.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        newDate.set(Calendar.MINUTE, minute)
                        continuation.resume(newDate.time)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    DateFormat.is24HourFormat(activity),
                )
                if (variable.title.isNotEmpty()) {
                    timePicker.setTitle(variable.title)
                }
                timePicker.setCancelable(true)
                timePicker.setCanceledOnTouchOutside(true)

                timePicker.showOrElse {
                    continuation.cancel()
                }
                timePicker.setOnDismissListener {
                    continuation.cancel()
                }
            }
        }
        if (variable.rememberValue) {
            variablesRepository.setVariableValue(variable.id, DATE_FORMAT.format(selectedDate.time))
        }
        return SimpleDateFormat(getTimeFormat(variable), Locale.US)
            .format(selectedDate.time)
    }

    private fun getInitialTime(previousValue: String?) =
        Calendar.getInstance()
            .applyIfNotNull(previousValue) {
                time = DATE_FORMAT.parseOrNull(it) ?: return@applyIfNotNull
            }

    companion object {

        const val KEY_FORMAT = "format"
        private const val DEFAULT_FORMAT = "HH:mm"

        private val DATE_FORMAT = SimpleDateFormat("HH-mm", Locale.US)

        fun getTimeFormat(variable: VariableModel) =
            variable.dataForType[DateType.KEY_FORMAT] ?: DEFAULT_FORMAT
    }
}
