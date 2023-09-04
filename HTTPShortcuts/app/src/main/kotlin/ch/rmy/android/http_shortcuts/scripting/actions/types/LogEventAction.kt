package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.history.HistoryEvent
import ch.rmy.android.http_shortcuts.history.HistoryEventLogger
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import javax.inject.Inject

class LogEventAction
@Inject
constructor(
    private val eventLogger: HistoryEventLogger,
) : Action<LogEventAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext) {
        eventLogger.logEvent(HistoryEvent.CustomEvent(title, message))
    }

    data class Params(
        val title: String,
        val message: String?,
    )
}
