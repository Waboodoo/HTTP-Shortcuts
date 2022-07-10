package ch.rmy.android.http_shortcuts.extensions

import ch.rmy.android.framework.utils.Destroyer
import ch.rmy.android.http_shortcuts.exceptions.CanceledByUserException
import io.reactivex.CompletableEmitter
import io.reactivex.SingleEmitter
import io.reactivex.disposables.Disposable

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

fun CompletableEmitter.createDestroyer(): Destroyer {
    val destroyer = Destroyer()
    setDisposable(object : Disposable {
        override fun dispose() {
            destroyer.destroy()
        }

        override fun isDisposed() =
            this@createDestroyer.isDisposed
    })
    return destroyer
}
