package ch.rmy.android.http_shortcuts.variables.types

import android.app.TimePickerDialog
import android.content.Context
import android.text.format.DateFormat
import ch.rmy.android.http_shortcuts.data.Commons
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.extensions.cancel
import ch.rmy.android.http_shortcuts.extensions.showIfPossible
import io.reactivex.Completable
import io.reactivex.Single
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

internal class TimeType : BaseVariableType(), AsyncVariableType {

    override val hasTitle = false

    override fun resolveValue(context: Context, variable: Variable): Single<String> =
        Single.create<Date> { emitter ->
            val calendar = getInitialTime(variable.value)
            val timePicker = TimePickerDialog(context, TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                val newDate = Calendar.getInstance()
                newDate.set(Calendar.HOUR_OF_DAY, hourOfDay)
                newDate.set(Calendar.MINUTE, minute)
                emitter.onSuccess(newDate.time)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), DateFormat.is24HourFormat(context))
            timePicker.setCancelable(true)
            timePicker.setCanceledOnTouchOutside(true)

            timePicker.showIfPossible()
            timePicker.setOnDismissListener {
                emitter.cancel()
            }
        }
            .flatMap { resolvedDate ->
                if (variable.rememberValue) {
                    Commons.setVariableValue(variable.id, DATE_FORMAT.format(resolvedDate.time))
                } else {
                    Completable.complete()
                }
                    .toSingle {
                        val dateFormat = SimpleDateFormat(variable.dataForType[DateType.KEY_FORMAT] ?: DEFAULT_FORMAT, Locale.US)
                        dateFormat.format(resolvedDate.time)
                    }
            }

    private fun getInitialTime(previousValue: String?) =
        Calendar.getInstance()
            .also {
                if (previousValue != null) {
                    try {
                        it.time = DATE_FORMAT.parse(previousValue)
                    } catch (e: ParseException) {
                    }
                }
            }

    override fun createEditorFragment() = TimeEditorFragment()

    companion object {

        const val KEY_FORMAT = "format"
        const val DEFAULT_FORMAT = "HH:mm"

        private val DATE_FORMAT = SimpleDateFormat("HH-mm", Locale.US)

    }

}
