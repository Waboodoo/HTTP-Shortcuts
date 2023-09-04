package ch.rmy.android.http_shortcuts.scripting.actions

import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.scripting.actions.types.Action

class ActionRunnable<Params : Any>(
    private val action: Action<Params>,
    private val params: Params,
) {
    suspend fun run(executionContext: ExecutionContext): Any? =
        action.executeWithParams(params, executionContext)
            .takeUnless { it == Unit }
}
