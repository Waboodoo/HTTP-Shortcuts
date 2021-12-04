package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import ch.rmy.android.http_shortcuts.R
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object StringUtils {

    fun getDurationText(context: Context, duration: Duration): CharSequence {
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
                context.resources.getQuantityString(R.plurals.seconds, seconds, seconds)
            )
        } else if (minutes > 0) {
            context.resources.getQuantityString(R.plurals.minutes, minutes, minutes)
        } else {
            context.resources.getQuantityString(R.plurals.seconds, seconds, seconds)
        }
    }

    fun getOrderedList(items: List<CharSequence>): CharSequence =
        SpannableStringBuilder()
            .apply {
                var offset = 0
                items.forEachIndexed { index, item ->
                    append(item)
                    var length = item.length
                    if (index != items.lastIndex) {
                        append("\n\n")
                        length += 2
                    }
                    setSpan(
                        OrderedListSpan(index + 1),
                        offset,
                        offset + length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
                    )
                    offset += length
                }
            }
}
