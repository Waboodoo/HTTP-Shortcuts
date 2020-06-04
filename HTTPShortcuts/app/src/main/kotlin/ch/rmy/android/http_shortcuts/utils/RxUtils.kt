package ch.rmy.android.http_shortcuts.utils

import androidx.annotation.CheckResult
import io.reactivex.Single

object RxUtils {

    @CheckResult
    fun <T> single(action: () -> T): Single<T> =
        Single.create { emitter ->
            try {
                val result = action.invoke()
                if (!emitter.isDisposed) {
                    emitter.onSuccess(result)
                }
            } catch (e: Throwable) {
                if (!emitter.isDisposed) {
                    emitter.onError(e)
                }
            }
        }

}