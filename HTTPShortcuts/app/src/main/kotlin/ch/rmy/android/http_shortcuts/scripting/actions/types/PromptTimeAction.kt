package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.app.TimePickerDialog
import android.content.Context
import android.text.format.DateFormat
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

class PromptTimeAction(
    private val format: String?,
    private val initialTime: String?,
) : BaseAction() {

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var activityProvider: ActivityProvider

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override suspend fun execute(executionContext: ExecutionContext): String? =
        withContext(Dispatchers.Main) {
            suspendCancellableCoroutine<String> { continuation ->
                val calendar = getInitialTime()
                val timePicker = TimePickerDialog(
                    activityProvider.getActivity(),
                    { _, hourOfDay, minute ->
                        val newDate = Calendar.getInstance()
                        newDate.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        newDate.set(Calendar.MINUTE, minute)
                        try {
                            continuation.resume(
                                SimpleDateFormat(format ?: DEFAULT_FORMAT, Locale.US).format(newDate.time.time)
                            )
                        } catch (e: IllegalArgumentException) {
                            continuation.resumeWithException(
                                UserException.create {
                                    getString(R.string.error_invalid_time_format)
                                }
                            )
                        }
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    DateFormat.is24HourFormat(context),
                )
                timePicker.setCancelable(true)
                timePicker.setCanceledOnTouchOutside(true)

                timePicker.showOrElse {
                    continuation.cancel()
                }
                timePicker.setOnDismissListener {
                    if (continuation.isActive) {
                        continuation.resume("")
                    }
                }
            }
        }
            .takeUnlessEmpty()
            ?.removePrefix("-")

    private fun getInitialTime(): Calendar {
        val calendar = Calendar.getInstance()
        if (!initialTime.isNullOrEmpty()) {
            DATE_FORMAT.parseOrNull(initialTime)?.let {
                calendar.time = it
            }
        }
        return calendar
    }

    companion object {
        private const val DEFAULT_FORMAT = "HH:mm"

        internal val DATE_FORMAT
            get() = SimpleDateFormat(DEFAULT_FORMAT, Locale.US)
    }
}
