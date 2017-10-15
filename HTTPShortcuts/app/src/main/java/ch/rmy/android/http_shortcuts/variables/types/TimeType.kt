package ch.rmy.android.http_shortcuts.variables.types

import android.app.TimePickerDialog
import android.content.Context
import android.text.format.DateFormat
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.realm.models.Variable
import ch.rmy.android.http_shortcuts.variables.Showable
import org.jdeferred.Deferred
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

internal class TimeType : BaseVariableType(), AsyncVariableType {

    override fun hasTitle() = false

    override fun createDialog(context: Context, controller: Controller, variable: Variable, deferredValue: Deferred<String, Void, Void>): Showable {
        val calendar = getInitialTime(variable.value)
        val timePicker = TimePickerDialog(context, TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            val newDate = Calendar.getInstance()
            newDate.set(Calendar.HOUR_OF_DAY, hourOfDay)
            newDate.set(Calendar.MINUTE, minute)
            if (deferredValue.isPending) {
                try {
                    val dateFormat = SimpleDateFormat(variable.dataForType?.get(KEY_FORMAT)?.toString() ?: DEFAULT_FORMAT)
                    deferredValue.resolve(dateFormat.format(newDate.time))
                    if (variable.rememberValue) {
                        controller.setVariableValue(variable, DATE_FORMAT.format(newDate.time))
                    }
                } catch (e: Exception) {
                    deferredValue.reject(null)
                }
            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), DateFormat.is24HourFormat(context))
        timePicker.setCancelable(true)
        timePicker.setCanceledOnTouchOutside(true)

        return {
            timePicker.show()
            timePicker.setOnDismissListener {
                if (deferredValue.isPending) {
                    deferredValue.reject(null)
                }
            }
        }
    }

    private fun getInitialTime(previousValue: String?): Calendar {
        val calendar = Calendar.getInstance()
        if (previousValue != null) {
            try {
                calendar.time = DATE_FORMAT.parse(previousValue)
            } catch (e: ParseException) {
            }
        }
        return calendar
    }

    override fun createEditorFragment() = TimeEditorFragment()

    companion object {

        const val KEY_FORMAT = "format"
        const val DEFAULT_FORMAT = "HH:mm"

        private val DATE_FORMAT = SimpleDateFormat("HH-mm")

    }

}
