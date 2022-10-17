package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import kotlinx.coroutines.delay
import kotlin.time.Duration

class WaitAction(private val duration: Duration) : BaseAction() {

    override suspend fun execute(executionContext: ExecutionContext) {
        delay(duration)
    }
}
