package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.history.HistoryEvent
import ch.rmy.android.http_shortcuts.history.HistoryEventLogger
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import javax.inject.Inject

class LogEventAction(
    private val title: String,
    private val message: String?,
) : BaseAction() {

    @Inject
    lateinit var eventLogger: HistoryEventLogger

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override suspend fun execute(executionContext: ExecutionContext) {
        eventLogger.logEvent(HistoryEvent.CustomEvent(title, message))
    }
}
