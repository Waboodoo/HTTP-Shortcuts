package ch.rmy.android.framework.extensions

import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

fun Continuation<Unit>.resume() {
    resume(Unit)
}
