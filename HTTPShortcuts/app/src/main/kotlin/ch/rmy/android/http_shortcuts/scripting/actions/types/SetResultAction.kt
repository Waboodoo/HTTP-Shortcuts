package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import javax.inject.Inject

class SetResultAction
@Inject
constructor() : Action<SetResultAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext) {
        executionContext.resultHandler.setResult(value)
    }

    data class Params(
        val value: String,
    )
}
