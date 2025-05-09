package ch.rmy.android.http_shortcuts.variables.types

import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.data.models.GlobalVariable
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class TimestampType
@Inject
constructor() : VariableType {
    override suspend fun resolve(variable: GlobalVariable, dialogHandle: DialogHandle): String =
        SimpleDateFormat(getTimeFormat(variable), Locale.getDefault())
            .format(Date.from(Instant.now()))

    companion object {

        const val KEY_FORMAT = "format"
        private const val DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss"

        fun getTimeFormat(variable: GlobalVariable) =
            variable.getStringData(DateType.KEY_FORMAT) ?: DEFAULT_FORMAT
    }
}
