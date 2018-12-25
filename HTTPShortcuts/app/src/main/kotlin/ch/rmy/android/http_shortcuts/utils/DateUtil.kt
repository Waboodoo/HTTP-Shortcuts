package ch.rmy.android.http_shortcuts.utils

import java.util.*

object DateUtil {

    fun calculateDate(delay: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MILLISECOND, delay)
        return calendar.time
    }

}