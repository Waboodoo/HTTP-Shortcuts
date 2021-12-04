package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class WaitAction(private val duration: Int) : BaseAction() {

    override fun execute(executionContext: ExecutionContext): Completable =
        Completable.timer(duration.toLong(), TimeUnit.MILLISECONDS, Schedulers.single())
}
