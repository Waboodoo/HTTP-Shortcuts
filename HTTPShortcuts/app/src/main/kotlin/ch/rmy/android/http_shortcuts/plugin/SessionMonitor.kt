package ch.rmy.android.http_shortcuts.plugin

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration

@Singleton
class SessionMonitor
@Inject
constructor() {

    private var startedDeferred: CompletableDeferred<Unit>? = null
    private var completedDeferred: CompletableDeferred<Unit>? = null

    suspend fun monitorSession(startTimeout: Duration, completionTimeout: Duration) {
        withTimeout(startTimeout) {
            startedDeferred?.await()
        }
        withTimeout(completionTimeout) {
            completedDeferred?.await()
        }
    }

    fun onSessionScheduled() {
        startedDeferred = CompletableDeferred()
        completedDeferred = CompletableDeferred()
    }

    fun onSessionStarted() {
        startedDeferred?.complete(Unit)
        startedDeferred = null
    }

    fun onSessionComplete() {
        completedDeferred?.complete(Unit)
        completedDeferred = null
    }
}
