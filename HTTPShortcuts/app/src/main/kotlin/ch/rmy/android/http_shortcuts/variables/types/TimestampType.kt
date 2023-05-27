package ch.rmy.android.http_shortcuts.variables.types

import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.data.models.Variable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class TimestampType : BaseVariableType() {

    override suspend fun resolveValue(variable: Variable, dialogHandle: DialogHandle): String =
        DateTimeFormatter.ofPattern(getTimeFormat(variable), Locale.getDefault())
            .format(LocalDateTime.now())

    companion object {

        const val KEY_FORMAT = "format"
        private const val DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss"

        fun getTimeFormat(variable: Variable) =
            variable.dataForType[DateType.KEY_FORMAT] ?: DEFAULT_FORMAT
    }
}
