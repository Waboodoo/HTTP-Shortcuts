package ch.rmy.android.http_shortcuts.utils

import android.os.SystemClock

object ActivityCloser {

    private var mainActivityLastClosed: Long? = null
    private var displayActivityLastClosed: Long? = null

    fun onMainActivityClosed() {
        mainActivityLastClosed = SystemClock.elapsedRealtime()
    }

    fun onDisplayResponseActivityClosed() {
        displayActivityLastClosed = SystemClock.elapsedRealtime()
    }

    fun onMainActivityDestroyed() {
        mainActivityLastClosed = null
    }

    fun shouldCloseMainActivity(): Boolean =
        mainActivityLastClosed != null && displayActivityLastClosed != null && mainActivityLastClosed!! < displayActivityLastClosed!!
}
