package ch.rmy.android.http_shortcuts.dialogs

import io.reactivex.Single

interface Dialog {

    fun shouldShow(): Boolean

    fun show(): Single<DialogResult>

    fun showIfNeeded(): Single<DialogResult> =
        if (shouldShow()) {
            show()
        } else {
            Single.just(DialogResult.NOT_SHOWN)
        }
}
