package ch.rmy.android.framework.utils.localization

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class DurationLocalizable(private val duration: Duration) : Localizable {
    override fun localize(context: Context): CharSequence {
        if (duration < 1.seconds) {
            return context.resources.getQuantityString(
                R.plurals.milliseconds,
                duration.inWholeMilliseconds.toInt(),
                duration.inWholeMilliseconds.toInt(),
            )
        }
        val minutes = duration.inWholeMinutes.toInt()
        val seconds = (duration - minutes.minutes).inWholeSeconds.toInt()
        return if (minutes > 0 && seconds > 0) {
            context.getString(
                R.string.pattern_minutes_seconds,
                context.resources.getQuantityString(R.plurals.minutes, minutes, minutes),
                context.resources.getQuantityString(R.plurals.seconds, seconds, seconds),
            )
        } else if (minutes > 0) {
            context.resources.getQuantityString(R.plurals.minutes, minutes, minutes)
        } else {
            context.resources.getQuantityString(R.plurals.seconds, seconds, seconds)
        }
    }
}
