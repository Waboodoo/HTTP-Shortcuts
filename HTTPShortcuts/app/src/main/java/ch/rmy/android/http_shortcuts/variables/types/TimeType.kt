package ch.rmy.android.http_shortcuts.variables.types

import android.app.TimePickerDialog
import android.content.Context
import android.text.format.DateFormat
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.realm.models.Variable
import ch.rmy.android.http_shortcuts.utils.rejectSafely
import ch.rmy.android.http_shortcuts.utils.showIfPossible
import org.jdeferred2.Deferred
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

internal class TimeType : BaseVariableType(), AsyncVariableType {

    override val hasTitle = false

    override fun createDialog(context: Context, controller: Controller, variable: Variable, deferredValue: Deferred<String, Unit, Unit>): () -> Unit {
        val calendar = getInitialTime(variable.value)
        val timePicker = TimePickerDialog(context, TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            val newDate = Calendar.getInstance()
            newDate.set(Calendar.HOUR_OF_DAY, hourOfDay)
            newDate.set(Calendar.MINUTE, minute)
            if (variable.isValid && deferredValue.isPending) {
                try {
                    val dateFormat = SimpleDateFormat(variable.dataForType[KEY_FORMAT]
                            ?: DEFAULT_FORMAT, Locale.US)
                    deferredValue.resolve(dateFormat.format(newDate.time))
                    if (variable.rememberValue) {
                        controller.setVariableValue(variable.id, DATE_FORMAT.format(newDate.time)).subscribe()
                    }
                } catch (e: Exception) {
                    deferredValue.rejectSafely(Unit)
                }
            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), DateFormat.is24HourFormat(context))
        timePicker.setCancelable(true)
        timePicker.setCanceledOnTouchOutside(true)

        return {
            timePicker.showIfPossible()
            timePicker.setOnDismissListener {
                deferredValue.rejectSafely(Unit)
            }
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
