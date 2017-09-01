package ch.rmy.android.http_shortcuts.variables.types

import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.realm.models.Variable
import ch.rmy.android.http_shortcuts.variables.Showable
import org.jdeferred.Deferred
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


internal class DateType : BaseVariableType(), AsyncVariableType {

    override fun hasTitle() = false

    override fun createDialog(context: Context, controller: Controller, variable: Variable, deferredValue: Deferred<String, Void, Void>): Showable {
        val calendar = getInitialDate(variable.value)
        val datePicker = DatePickerDialog(context, null, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        datePicker.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.button_ok),
                { _, _ ->
                    val newDate = Calendar.getInstance()
                    val day = datePicker.getDatePicker().getDayOfMonth()
                    val month = datePicker.getDatePicker().getMonth()
                    val year = datePicker.getDatePicker().getYear()
                    newDate.set(year, month, day)
                    if (deferredValue.isPending) {
                        deferredValue.resolve(newDate.getTime().toString()) // TODO: Add formats
                        controller.setVariableValue(variable, DATE_FORMAT.format(newDate.time))
                    }
                })
        datePicker.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.button_cancel), { _, _ -> })
        datePicker.setCancelable(true)
        datePicker.setCanceledOnTouchOutside(true)
        datePicker.setOnDismissListener {
            if (deferredValue.isPending) {
                deferredValue.reject(null)
            }
        }

        return object : Showable {
            override fun show() {
                datePicker.show()
            }
        }
    }

    private fun getInitialDate(previousValue: String?): Calendar {
        val calendar = Calendar.getInstance()
        if (previousValue != null) {
            try {
                calendar.time = DATE_FORMAT.parse(previousValue)
            } catch (e: ParseException) {
            }
        }
        return calendar
    }

    override fun createEditorFragment() = TextEditorFragment()

    companion object {

        private val DATE_FORMAT = SimpleDateFormat("yyyy-mm-dd")

    }

}
