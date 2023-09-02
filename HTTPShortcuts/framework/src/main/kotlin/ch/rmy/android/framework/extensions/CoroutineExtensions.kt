package ch.rmy.android.framework.extensions

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

fun Continuation<Unit>.resume() {
    resume(Unit)
}

suspend fun <T : Any> Flow<T?>.awaitNonNull(): T =
    first { it != null }!!