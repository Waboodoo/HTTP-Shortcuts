package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext

abstract class BaseAction {

    abstract suspend fun execute(executionContext: ExecutionContext): Any?

    protected open fun inject(applicationComponent: ApplicationComponent) {
        // intentionally left blank
    }

    suspend fun run(executionContext: ExecutionContext): Any? {
        inject(executionContext.context.getApplicationComponent())
        return execute(executionContext)
            .takeUnless { it == Unit }
    }
}
