package ch.rmy.android.framework.utils

import java.util.Calendar
import java.util.Date

object DateUtil {

    fun calculateDate(delay: Int): Date? {
        if (delay == 0) {
            return null
        }
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MILLISECOND, delay)
        return calendar.time
    }
}
