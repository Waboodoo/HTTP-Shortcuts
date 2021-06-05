package ch.rmy.android.http_shortcuts.plugin

import java.util.concurrent.TimeoutException

object SessionMonitor {

    private var sessionInProgress = false

    fun startAndMonitorSession(timeout: Int) {
        sessionInProgress = true
        val start = System.currentTimeMillis()
        while (sessionInProgress) {
            Thread.sleep(300)
            if (System.currentTimeMillis() - start > timeout) {
                throw TimeoutException()
            }
        }
    }

    fun onSessionComplete() {
        sessionInProgress = false
    }

}