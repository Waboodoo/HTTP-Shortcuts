package ch.rmy.android.http_shortcuts.extensions

import ch.rmy.android.http_shortcuts.exceptions.CanceledByUserException
import kotlinx.coroutines.CancellableContinuation
import kotlin.coroutines.resumeWithException

fun <T> CancellableContinuation<T>.canceledByUser() {
    if (isActive) {
        resumeWithException(CanceledByUserException())
    }
}
