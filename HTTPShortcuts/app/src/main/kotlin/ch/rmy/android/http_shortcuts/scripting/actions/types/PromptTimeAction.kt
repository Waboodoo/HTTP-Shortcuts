package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogState
import ch.rmy.android.http_shortcuts.exceptions.DialogCancellationException
import ch.rmy.android.http_shortcuts.exceptions.UserException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class PromptTimeAction
@Inject
constructor() : Action<PromptTimeAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext): String? =
        try {
            val selectedTime = executionContext.dialogHandle.showDialog(
                ExecuteDialogState.TimePicker(
                    title = title,
                    initialTime = getInitialTime(),
                ),
            )
            val pattern = format ?: DEFAULT_FORMAT
            try {
                SimpleDateFormat(pattern, Locale.US)
                    .format(Date.from(LocalDate.now().atTime(selectedTime).atZone(ZoneOffset.systemDefault()).toInstant()))
            } catch (e: IllegalArgumentException) {
                throw UserException.create {
                    getString(R.string.error_invalid_time_format)
                }
            }
        } catch (_: DialogCancellationException) {
            null
        }

    private fun Params.getInitialTime(): LocalTime =
        initialTime
            ?.takeUnlessEmpty()
            ?.let { timeString ->
                try {
                    LocalTime.parse(timeString, DateTimeFormatter.ofPattern(DEFAULT_FORMAT, Locale.US))
                } catch (e: DateTimeParseException) {
                    null
                }
            }
            ?: LocalTime.now()

    data class Params(
        val format: String?,
        val initialTime: String?,
        val title: String?,
    )

    companion object {
        private const val DEFAULT_FORMAT = "HH:mm"
    }
}
