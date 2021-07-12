package ch.rmy.android.http_shortcuts.plugin

import java.util.concurrent.TimeoutException

object SessionMonitor {

    private var sessionStatus: SessionStatus = SessionStatus.SCHEDULED

    fun onSessionScheduled() {
        sessionStatus = SessionStatus.SCHEDULED
    }

    fun monitorSession(startTimeout: Int, completionTimeout: Int) {
        val start = System.currentTimeMillis()
        while (sessionStatus == SessionStatus.SCHEDULED) {
            Thread.sleep(300)
            if (System.currentTimeMillis() - start > startTimeout) {
                throw SessionStartException()
            }
        }
        while (sessionStatus != SessionStatus.COMPLETED) {
            Thread.sleep(300)
            if (System.currentTimeMillis() - start > completionTimeout) {
                throw TimeoutException()
            }
        }
    }

    fun onSessionStarted() {
        sessionStatus = SessionStatus.RUNNING
    }

    fun onSessionComplete() {
        sessionStatus = SessionStatus.COMPLETED
    }

    enum class SessionStatus {
        SCHEDULED,
        RUNNING,
        COMPLETED,
    }

    class SessionStartException : Exception()

}