package ch.rmy.android.framework.utils

import android.os.SystemClock

fun interface ElapsedTimeProvider {
    fun get(): ElapsedTime

    companion object {
        val default: ElapsedTimeProvider = {
            ElapsedTime(SystemClock.elapsedRealtime())
        }
    }
}
