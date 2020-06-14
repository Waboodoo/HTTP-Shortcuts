package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import io.reactivex.Completable
import io.reactivex.Single

abstract class BaseAction {

    open fun execute(executionContext: ExecutionContext): Completable =
        Completable.complete()

    open fun executeForValue(executionContext: ExecutionContext): Single<String> =
        execute(executionContext).toSingleDefault(NO_RESULT)

    companion object {

        const val NO_RESULT = "[[[no result]]]"

    }

}