package ch.rmy.android.framework.utils

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@JvmInline
value class ElapsedTime(private val elapsedRealtime: Long) : Comparable<ElapsedTime> {
    override fun compareTo(other: ElapsedTime): Int =
        elapsedRealtime.compareTo(other.elapsedRealtime)

    operator fun plus(other: Duration) =
        ElapsedTime(elapsedRealtime + other.inWholeMilliseconds)

    operator fun minus(other: ElapsedTime) =
        (elapsedRealtime - other.elapsedRealtime).milliseconds

    companion object {
        val MIN = ElapsedTime(0L)
    }
}
