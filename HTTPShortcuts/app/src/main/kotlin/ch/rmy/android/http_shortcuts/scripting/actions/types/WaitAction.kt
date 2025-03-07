package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import javax.inject.Inject
import kotlin.time.Duration
import kotlinx.coroutines.delay

class WaitAction
@Inject
constructor() : Action<WaitAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext) {
        delay(duration)
    }

    data class Params(
        val duration: Duration,
    )
}
