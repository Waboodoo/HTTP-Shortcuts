package ch.rmy.android.framework.extensions

import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

operator fun Instant.minus(other: Instant): Duration =
    (toEpochMilli() - other.toEpochMilli()).milliseconds

operator fun Instant.minus(duration: Duration): Instant =
    minus(duration.inWholeMilliseconds, ChronoUnit.MILLIS)

operator fun Instant.plus(duration: Duration): Instant =
    plus(duration.inWholeMilliseconds, ChronoUnit.MILLIS)
