package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import io.reactivex.Completable
import io.reactivex.Single

abstract class BaseAction {

    open fun execute(executionContext: ExecutionContext): Completable =
        Completable.complete()

    open fun executeForValue(executionContext: ExecutionContext): Single<Any> =
        execute(executionContext).toSingleDefault(NO_RESULT)

    protected open fun inject(applicationComponent: ApplicationComponent) {
        // intentionally left blank
    }

    fun run(executionContext: ExecutionContext): Single<Any> {
        inject(executionContext.context.getApplicationComponent())
        return executeForValue(executionContext)
    }

    companion object {

        const val NO_RESULT = "[[[no result]]]"
    }
}
