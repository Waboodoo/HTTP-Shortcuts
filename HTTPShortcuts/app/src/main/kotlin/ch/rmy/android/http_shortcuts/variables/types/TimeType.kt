package ch.rmy.android.http_shortcuts.variables.types

import android.app.TimePickerDialog
import android.content.Context
import android.text.format.DateFormat
import ch.rmy.android.framework.extensions.showIfPossible
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.extensions.cancel
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

internal class TimeType : BaseVariableType() {

    private val variablesRepository = VariableRepository()

    override fun resolveValue(context: Context, variable: Variable): Single<String> =
        Single.create<Date> { emitter ->
            val calendar = getInitialTime(variable.value)
            val timePicker = TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    val newDate = Calendar.getInstance()
                    newDate.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    newDate.set(Calendar.MINUTE, minute)
                    emitter.onSuccess(newDate.time)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(context),
            )
            timePicker.setCancelable(true)
            timePicker.setCanceledOnTouchOutside(true)

            timePicker.showIfPossible()
            timePicker.setOnDismissListener {
                emitter.cancel()
            }
        }
            .subscribeOn(AndroidSchedulers.mainThread())
            .flatMap { resolvedDate ->
                if (variable.rememberValue) {
                    variablesRepository.setVariableValue(variable.id, DATE_FORMAT.format(resolvedDate.time))
                } else {
                    Completable.complete()
                }
                    .toSingle {
                        SimpleDateFormat(getTimeFormat(variable), Locale.US)
                            .format(resolvedDate.time)
                    }
            }

    private fun getInitialTime(previousValue: String?) =
        Calendar.getInstance()
            .also {
                if (previousValue != null) {
                    try {
                        it.time = DATE_FORMAT.parse(previousValue)!!
                    } catch (e: ParseException) {
                    }
                }
            }

    companion object {

        const val KEY_FORMAT = "format"
        private const val DEFAULT_FORMAT = "HH:mm"

        private val DATE_FORMAT = SimpleDateFormat("HH-mm", Locale.US)

        fun getTimeFormat(variable: Variable) =
            variable.dataForType[DateType.KEY_FORMAT] ?: DEFAULT_FORMAT
    }
}
