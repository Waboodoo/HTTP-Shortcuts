package ch.rmy.android.http_shortcuts.utils

import android.os.Handler
import org.jdeferred2.Promise
import org.jdeferred2.impl.DeferredObject

object PromiseUtils {

    /**
     * Returns a promise that is immediately resolved with the provided value.
     */
    fun <T, U, V> resolve(item: T?): Promise<T, U, V> =
            DeferredObject<T, U, V>()
                    .also {
                        it.resolve(item)
                    }.promise()

    /**
     * Returns a promise that is immediately rejected with the provided value.
     */
    fun <T, U, V> reject(item: U?): Promise<T, U, V> =
            DeferredObject<T, U, V>()
                    .also {
                        it.reject(item)
                    }.promise()

    fun <T, U, V> resolveDelayed(item: T?, delay: Long): Promise<T, U, V> =
            DeferredObject<T, U, V>()
                    .also {
                        Handler().postDelayed({
                            it.resolve(item)
                        }, delay)
                    }.promise()

}
