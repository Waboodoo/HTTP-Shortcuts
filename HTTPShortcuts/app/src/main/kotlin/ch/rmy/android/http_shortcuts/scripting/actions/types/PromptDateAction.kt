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

class PromptDateAction(
    private val format: String?,
    private val initialDate: String?,
) : BaseAction() {

    override suspend fun execute(executionContext: ExecutionContext): String? =
        try {
            val selectedDate = executionContext.dialogHandle.showDialog(
                ExecuteDialogState.DatePicker(
                    initialDate = getInitialDate(),
                )
            )
            try {
                SimpleDateFormat(format ?: DEFAULT_FORMAT, Locale.getDefault())
                    .format(Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant()))
            } catch (e: IllegalArgumentException) {
                throw UserException.create {
                    getString(R.string.error_invalid_date_format)
                }
            }
        } catch (e: DialogCancellationException) {
            null
        }

    private fun getInitialDate(): LocalDate =
        initialDate
            ?.takeUnlessEmpty()
            ?.let { dateString ->
                try {
                    LocalDate.parse(dateString, DateTimeFormatter.ofPattern(DEFAULT_FORMAT, Locale.US))
                } catch (e: DateTimeParseException) {
                    null
                }
            }
            ?: LocalDate.now()

    companion object {
        private const val DEFAULT_FORMAT = "yyyy-MM-dd"
    }
}
