package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogState
import ch.rmy.android.http_shortcuts.exceptions.DialogCancellationException
import ch.rmy.android.http_shortcuts.exceptions.UserException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class PromptDateAction
@Inject
constructor() : Action<PromptDateAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext): String? =
        try {
            val selectedDate = executionContext.dialogHandle.showDialog(
                ExecuteDialogState.DatePicker(
                    title = title,
                    initialDate = getInitialDate(),
                ),
            )
            try {
                SimpleDateFormat(format ?: DEFAULT_FORMAT, Locale.getDefault())
                    .format(Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant()))
            } catch (_: IllegalArgumentException) {
                throw UserException.create {
                    getString(R.string.error_invalid_date_format)
                }
            }
        } catch (_: DialogCancellationException) {
            null
        }

    private fun Params.getInitialDate(): LocalDate =
        initialDate
            ?.takeUnlessEmpty()
            ?.let { dateString ->
                try {
                    LocalDate.parse(dateString, DateTimeFormatter.ofPattern(DEFAULT_FORMAT, Locale.US))
                } catch (_: DateTimeParseException) {
                    null
                }
            }
            ?: LocalDate.now()

    data class Params(
        val format: String?,
        val initialDate: String?,
        val title: String?,
    )

    companion object {
        private const val DEFAULT_FORMAT = "yyyy-MM-dd"
    }
}
