package ch.rmy.android.http_shortcuts.variables.types

import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.Commons
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.extensions.cancel
import ch.rmy.android.http_shortcuts.extensions.showIfPossible
import io.reactivex.Completable
import io.reactivex.Single
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


internal class DateType : BaseVariableType(), AsyncVariableType {

    override val hasTitle = false

    override fun resolveValue(context: Context, variable: Variable): Single<String> =
        Single.create<Date> { emitter ->
            val calendar = getInitialDate(variable.value)
            val datePicker = DatePickerDialog(context, null, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            datePicker.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.dialog_ok)) { _, _ ->
                val newDate = Calendar.getInstance()
                val day = datePicker.datePicker.dayOfMonth
                val month = datePicker.datePicker.month
                val year = datePicker.datePicker.year
                newDate.set(year, month, day)
                emitter.onSuccess(newDate.time)
            }
            datePicker.setCancelable(true)
            datePicker.setCanceledOnTouchOutside(true)

            datePicker.showIfPossible()
                ?: run {
                    emitter.cancel()
                }
            datePicker.setOnDismissListener {
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
                        val dateFormat = SimpleDateFormat(variable.dataForType[KEY_FORMAT] ?: DEFAULT_FORMAT, Locale.US)
                        dateFormat.format(resolvedDate.time)
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

    override fun createEditorFragment() = DateEditorFragment()

    companion object {

        const val KEY_FORMAT = "format"
        const val DEFAULT_FORMAT = "yyyy-MM-dd"

        private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    }

}
