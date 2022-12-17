package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ExecutionContext

class SetResultAction(val value: String) : BaseAction() {
    override suspend fun execute(executionContext: ExecutionContext) {
        executionContext.resultHandler.setResult(value)
    }
}
