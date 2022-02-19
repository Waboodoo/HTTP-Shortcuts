package ch.rmy.android.http_shortcuts.activities.variables.editor.types.date

import android.app.Application
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.BaseVariableTypeViewModel
import ch.rmy.android.http_shortcuts.variables.types.DateType
import java.text.SimpleDateFormat
import java.util.Locale

class DateTypeViewModel(application: Application) : BaseVariableTypeViewModel<Unit, DateTypeViewState>(application) {

    override fun initViewState() = DateTypeViewState(
        dateFormat = DateType.getDateFormat(variable),
        rememberValue = variable.rememberValue,
    )

    fun onDateFormatChanged(dateFormat: String) {
        performOperation(
            temporaryVariableRepository.setDataForType(
                mapOf(DateType.KEY_FORMAT to dateFormat)
            )
        )
    }

    fun onRememberValueChanged(enabled: Boolean) {
        performOperation(
            temporaryVariableRepository.setRememberValue(enabled)
        )
    }

    override fun validate(): Boolean {
        try {
            SimpleDateFormat(DateType.getDateFormat(variable), Locale.US)
        } catch (e: IllegalArgumentException) {
            showSnackbar(R.string.error_invalid_date_format)
            return false
        }
        return true
    }
}
