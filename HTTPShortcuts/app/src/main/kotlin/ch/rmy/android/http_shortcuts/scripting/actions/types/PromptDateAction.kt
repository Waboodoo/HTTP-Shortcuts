package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.app.DatePickerDialog
import android.content.DialogInterface
import ch.rmy.android.framework.extensions.showOrElse
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.exceptions.UserException
import ch.rmy.android.http_shortcuts.extensions.parseOrNull
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
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
                val calendar = getInitialDate()
                val activity = activityProvider.getActivity()
                val datePicker = DatePickerDialog(
                    activity,
                    null,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH),
                )
                datePicker.setButton(
                    DialogInterface.BUTTON_POSITIVE,
                    activity.getString(R.string.dialog_ok),
                ) { _, _ ->
                    val newDate = Calendar.getInstance()
                    val day = datePicker.datePicker.dayOfMonth
                    val month = datePicker.datePicker.month
                    val year = datePicker.datePicker.year
                    newDate.set(year, month, day)

                    try {
                        continuation.resume(
                            SimpleDateFormat(format ?: DEFAULT_FORMAT, Locale.US).format(newDate.time.time)
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

    private fun getInitialDate(): Calendar {
        val calendar = Calendar.getInstance()
        if (!initialDate.isNullOrEmpty()) {
            DATE_FORMAT.parseOrNull(initialDate)?.let {
                calendar.time = it
            }
        }
        return calendar
    }

    companion object {
        private const val DEFAULT_FORMAT = "yyyy-MM-dd"

        internal val DATE_FORMAT
            get() = SimpleDateFormat(DEFAULT_FORMAT, Locale.US)
    }
}
