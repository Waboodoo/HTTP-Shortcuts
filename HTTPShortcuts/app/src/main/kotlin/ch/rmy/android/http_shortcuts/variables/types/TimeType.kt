package ch.rmy.android.http_shortcuts.variables.types

import android.app.TimePickerDialog
import android.text.format.DateFormat
import ch.rmy.android.framework.extensions.showOrElse
import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
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

    override suspend fun resolveValue(variable: Variable, dialogHandle: DialogHandle): String {
        val selectedTime = withContext(Dispatchers.Main) {
            suspendCancellableCoroutine<LocalTime> { continuation ->
                val time = getInitialTime(variable.value.takeIf { variable.rememberValue })
                val activity = activityProvider.getActivity()
                val timePicker = TimePickerDialog(
                    activity,
                    { _, hourOfDay, minute ->
                        continuation.resume(LocalTime.of(hourOfDay, minute))
                    },
                    time.hour,
                    time.minute,
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
