package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import ch.rmy.android.http_shortcuts.R

object StringUtils {

    fun getDurationText(context: Context, durationInMilliseconds: Int): CharSequence {
        if (durationInMilliseconds < 1000) {
            return context.resources.getQuantityString(
                R.plurals.milliseconds,
                durationInMilliseconds,
                durationInMilliseconds
            )
        }
        val durationInSeconds = durationInMilliseconds / 1000
        val minutes = durationInSeconds / 60
        val seconds = durationInSeconds % 60
        return if (minutes > 0 && seconds > 0) {
            context.getString(R.string.pattern_minutes_seconds,
                context.resources.getQuantityString(R.plurals.minutes, minutes, minutes),
                context.resources.getQuantityString(R.plurals.seconds, seconds, seconds)
            )
        } else if (minutes > 0) {
            context.resources.getQuantityString(R.plurals.minutes, minutes, minutes)
        } else {
            context.resources.getQuantityString(R.plurals.seconds, seconds, seconds)
        }
    }

}