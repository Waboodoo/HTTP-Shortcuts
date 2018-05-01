package ch.rmy.android.http_shortcuts.utils

import android.os.Handler
import org.jdeferred2.Promise
import org.jdeferred2.impl.DeferredObject

object PromiseUtils {

    /**
     * Returns a promise that is immediately resolved with the provided value.
     */
    fun <T, U, V> resolve(item: T?): Promise<T, U, V> {
        val deferred = DeferredObject<T, U, V>()
        deferred.resolve(item)
        return deferred.promise()
    }

    /**
     * Returns a promise that is immediately rejected with the provided value.
     */
    fun <T, U, V> reject(item: U?): Promise<T, U, V> {
        val deferred = DeferredObject<T, U, V>()
        deferred.reject(item)
        return deferred.promise()
    }

    fun <T, U, V> resolveDelayed(item: T?, delay: Long): Promise<T, U, V> {
        val deferred = DeferredObject<T, U, V>()
        Handler().postDelayed({
            deferred.resolve(item)
        }, delay)
        return deferred.promise()
    }

}
