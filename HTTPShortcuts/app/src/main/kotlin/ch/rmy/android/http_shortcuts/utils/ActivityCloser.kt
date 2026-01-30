package ch.rmy.android.http_shortcuts.utils

import ch.rmy.android.framework.utils.ElapsedTime
import ch.rmy.android.framework.utils.ElapsedTimeProvider

object ActivityCloser {

    private var mainActivityLastClosed: ElapsedTime? = null
    private var displayActivityLastClosed: ElapsedTime? = null

    fun onMainActivityClosed() {
        mainActivityLastClosed = ElapsedTimeProvider.default.get()
    }

    fun onDisplayResponseActivityClosed() {
        displayActivityLastClosed = ElapsedTimeProvider.default.get()
    }

    fun onMainActivityDestroyed() {
        mainActivityLastClosed = null
    }

    fun shouldCloseMainActivity(): Boolean =
        mainActivityLastClosed != null && displayActivityLastClosed != null && mainActivityLastClosed!! < displayActivityLastClosed!!
}
