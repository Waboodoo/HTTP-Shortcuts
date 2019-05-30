package ch.rmy.android.http_shortcuts.extensions

import ch.rmy.android.http_shortcuts.utils.CanceledByUserException
import io.reactivex.CompletableEmitter
import io.reactivex.SingleEmitter

fun CompletableEmitter.cancel() {
    if (!isDisposed) {
        onError(CanceledByUserException())
    }
}

fun <T> SingleEmitter<T>.cancel() {
    if (!isDisposed) {
        onError(CanceledByUserException())
    }
}