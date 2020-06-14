package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import io.reactivex.Completable
import java.util.concurrent.TimeUnit

class WaitAction(data: Map<String, String>) : BaseAction() {

    private val duration = data[KEY_DURATION]?.toLongOrNull() ?: 0

    override fun execute(executionContext: ExecutionContext): Completable =
        Completable.complete().delay(duration, TimeUnit.MILLISECONDS)

    companion object {

        const val KEY_DURATION = "duration"

    }

}