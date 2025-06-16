package ch.rmy.android.http_shortcuts.activities.execute.usecases

import ch.rmy.android.framework.extensions.minus
import ch.rmy.android.framework.extensions.plus
import java.time.Instant
import javax.inject.Inject
import kotlin.math.roundToLong
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class GetNextRepetitionTimeUseCase
@Inject
constructor(
    private val now: () -> Instant,
) {
    operator fun invoke(triggeredAt: Instant, interval: Duration): Instant {
        val now = now()
        val elapsedMinutes = (now - triggeredAt).inWholeMinutes.coerceAtLeast(0)
        var nextTime = triggeredAt + interval * ((elapsedMinutes / interval.inWholeMinutes) + 1).toInt()
        if (nextTime - now < interval * 0.2) {
            nextTime += interval
        }
        return nextTime.roundTo(5.minutes)
    }

    private fun Instant.roundTo(precision: Duration): Instant =
        Instant.ofEpochMilli(
            (toEpochMilli() / precision.inWholeMilliseconds.toDouble()).roundToLong() * precision.inWholeMilliseconds,
        )
}
