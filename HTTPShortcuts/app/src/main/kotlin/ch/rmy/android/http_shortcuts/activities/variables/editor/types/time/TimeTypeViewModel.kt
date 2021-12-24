package ch.rmy.android.http_shortcuts.activities.variables.editor.types.time

import android.app.Application
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.BaseVariableTypeViewModel
import ch.rmy.android.http_shortcuts.variables.types.TimeType
import java.text.SimpleDateFormat
import java.util.Locale

class TimeTypeViewModel(application: Application) : BaseVariableTypeViewModel<Unit, TimeTypeViewState>(application) {

    override fun initViewState() = TimeTypeViewState(
        timeFormat = variable.dataForType[TimeType.KEY_FORMAT] ?: TimeType.DEFAULT_FORMAT,
        rememberValue = variable.rememberValue,
    )

    fun onTimeFormatChanged(timeFormat: String) {
        performOperation(
            temporaryVariableRepository.setDataForType(
                mapOf(TimeType.KEY_FORMAT to timeFormat)
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
            SimpleDateFormat(variable.dataForType[TimeType.KEY_FORMAT], Locale.US)
        } catch (e: IllegalArgumentException) {
            showSnackbar(R.string.error_invalid_time_format)
            return false
        }
        return true
    }
}
