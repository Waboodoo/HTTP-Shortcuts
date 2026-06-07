package ch.rmy.android.http_shortcuts.variables.types

import ch.rmy.android.framework.extensions.applyIf
import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.data.models.GlobalVariable
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

class TimestampType
@Inject
constructor() : VariableType {
    override suspend fun resolve(variable: GlobalVariable, dialogHandle: DialogHandle): String =
        SimpleDateFormat(getTimeFormat(variable), Locale.getDefault())
            .applyIf(getTimeZone(variable) == TIMEZONE_UTC) {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            .format(Date.from(Instant.now()))

    companion object {

        const val KEY_FORMAT = "format"
        const val KEY_TIMEZONE = "timezone"
        private const val DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss"

        const val TIMEZONE_UTC = "UTC"

        fun getTimeFormat(variable: GlobalVariable) =
            variable.getStringData(KEY_FORMAT) ?: DEFAULT_FORMAT

        fun getTimeZone(variable: GlobalVariable) =
            variable.getStringData(KEY_TIMEZONE)
    }
}
