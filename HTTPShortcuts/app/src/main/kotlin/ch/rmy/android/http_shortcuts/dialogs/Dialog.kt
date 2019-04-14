package ch.rmy.android.http_shortcuts.dialogs

import io.reactivex.Completable

interface Dialog {

    fun shouldShow(): Boolean

    fun show(): Completable

    fun showIfNeeded(): Completable =
        if (shouldShow()) {
            show()
        } else {
            Completable.complete()
        }

}