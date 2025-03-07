package ch.rmy.android.http_shortcuts.plugin

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeout

@Singleton
class SessionMonitor
@Inject
constructor() {

    private var startedDeferred: CompletableDeferred<Unit>? = null
    private var result: String? = null
    private var completedDeferred: CompletableDeferred<String?>? = null

    suspend fun monitorSession(startTimeout: Duration, completionTimeout: Duration): String {
        withTimeout(startTimeout) {
            startedDeferred?.await()
        }
        return withTimeout(completionTimeout) {
            completedDeferred?.await().orEmpty()
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

    fun onResult(result: String?) {
        this.result = result
    }

    fun onSessionComplete() {
        completedDeferred?.complete(result)
        completedDeferred = null
        result = null
    }
}
