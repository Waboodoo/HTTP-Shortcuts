package ch.rmy.android.http_shortcuts.activities.variables.editor.types.time

import android.app.Application
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.BaseVariableTypeViewModel
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.variables.types.TimeType
import java.text.SimpleDateFormat
import java.util.Locale

class TimeTypeViewModel(application: Application) : BaseVariableTypeViewModel<Unit, TimeTypeViewState>(application) {

    init {
        getApplicationComponent().inject(this)
    }

    override fun initViewState() = TimeTypeViewState(
        timeFormat = TimeType.getTimeFormat(variable),
        rememberValue = variable.rememberValue,
    )

    fun onTimeFormatChanged(timeFormat: String) {
        launchWithProgressTracking {
            temporaryVariableRepository.setDataForType(
                mapOf(TimeType.KEY_FORMAT to timeFormat)
            )
        }
    }

    fun onRememberValueChanged(enabled: Boolean) {
        launchWithProgressTracking {
            temporaryVariableRepository.setRememberValue(enabled)
        }
    }

    override fun validate(): Boolean {
        try {
            SimpleDateFormat(TimeType.getTimeFormat(variable), Locale.US)
        } catch (e: IllegalArgumentException) {
            showSnackbar(R.string.error_invalid_time_format)
            return false
        }
        return true
    }
}
