package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ExecutionContext

interface Action<Params : Any> {
    suspend fun executeWithParams(params: Params, executionContext: ExecutionContext): Any? =
        params.run {
            execute(executionContext)
        }

    suspend fun Params.execute(executionContext: ExecutionContext): Any?
}
