package ch.rmy.android.http_shortcuts.utils

import org.jdeferred.Promise
import org.jdeferred.impl.DeferredObject

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

}
