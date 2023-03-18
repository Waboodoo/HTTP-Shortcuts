package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.app.DatePickerDialog
import android.content.DialogInterface
import ch.rmy.android.framework.extensions.showOrElse
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.exceptions.UserException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class PromptDateAction(
    private val format: String?,
    private val initialDate: String?,
) : BaseAction() {

    @Inject
    lateinit var activityProvider: ActivityProvider

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override suspend fun execute(executionContext: ExecutionContext): String? =
        withContext(Dispatchers.Main) {
            suspendCancellableCoroutine<String> { continuation ->
                val date = getInitialDate()
                val activity = activityProvider.getActivity()
                val datePicker = DatePickerDialog(
                    activity,
                    null,
                    date.year,
                    date.monthValue,
                    date.dayOfMonth,
                )
                datePicker.setButton(
                    DialogInterface.BUTTON_POSITIVE,
                    activity.getString(R.string.dialog_ok),
                ) { _, _ ->
                    val newDate = LocalDate.of(
                        datePicker.datePicker.year,
                        datePicker.datePicker.month,
                        datePicker.datePicker.dayOfMonth,
                    )
                    try {
                        continuation.resume(
                            DateTimeFormatter.ofPattern(format ?: DEFAULT_FORMAT, Locale.US)
                                .format(newDate)
                        )
                    } catch (e: IllegalArgumentException) {
                        continuation.resumeWithException(
                            UserException.create {
                                getString(R.string.error_invalid_date_format)
                            }
                        )
                    }
                }
                datePicker.setCancelable(true)
                datePicker.setCanceledOnTouchOutside(true)

                datePicker.showOrElse {
                    continuation.cancel()
                }
                datePicker.setOnDismissListener {
                    if (continuation.isActive) {
                        continuation.resume("")
                    }
                }
            }
        }
            .takeUnlessEmpty()
            ?.removePrefix("-")

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
