package ch.rmy.android.framework.utils

import androidx.annotation.CheckResult
import io.reactivex.Single

object RxUtils {

    @CheckResult
    fun <T : Any> single(action: () -> T): Single<T> =
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
