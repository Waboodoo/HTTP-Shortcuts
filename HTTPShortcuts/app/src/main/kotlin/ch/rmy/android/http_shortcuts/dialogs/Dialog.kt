package ch.rmy.android.http_shortcuts.dialogs

import io.reactivex.Single

@Deprecated("Split dialogs into DialogState and a helper class for determining whether it should be shown")
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
