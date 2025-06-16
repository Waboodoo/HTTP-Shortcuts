package ch.rmy.android.http_shortcuts.activities.execute.usecases

import ch.rmy.android.framework.extensions.minus
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class GetNextRepetitionTimeUseCaseTest {

    private val now = Instant.ofEpochMilli(1.days.inWholeMilliseconds)
    private val useCase = GetNextRepetitionTimeUseCase(
        now = { now },
    )

    @Test
    fun `first interval`() {
        val nextTime = useCase.invoke(
            triggeredAt = now,
            interval = 15.minutes,
        ) - now

        assertEquals(15.minutes, nextTime)
    }

    @Test
    fun `first interval, with rounding`() {
        val nextTime = useCase.invoke(
            triggeredAt = now - 55.seconds,
            interval = 15.minutes,
        ) - now

        assertEquals(15.minutes, nextTime)
    }

    @Test
    fun `third interval`() {
        val nextTime = useCase.invoke(
            triggeredAt = now - 45.minutes,
            interval = 15.minutes,
        ) - now

        assertEquals(15.minutes, nextTime)
    }

    @Test
    fun `third and a half interval`() {
        val nextTime = useCase.invoke(
            triggeredAt = now - 52.minutes,
            interval = 15.minutes,
        ) - now

        assertEquals(10.minutes, nextTime)
    }

    @Test
    fun `too close to fourth interval`() {
        val nextTime = useCase.invoke(
            triggeredAt = now - 59.minutes,
            interval = 15.minutes,
        ) - now

        assertEquals(15.minutes, nextTime)
    }
}
