package ch.rmy.android.http_shortcuts.realm

import ch.rmy.android.http_shortcuts.utils.logException
import ch.rmy.android.http_shortcuts.utils.rejectSafely
import io.reactivex.Completable
import io.realm.Realm
import io.realm.RealmObject
import org.jdeferred2.Promise
import org.jdeferred2.android.AndroidDeferredObject

fun Realm.commitAsync(transaction: (realm: Realm) -> Unit): Promise<Unit, Throwable, Unit> {
    val deferred = AndroidDeferredObject<Unit, Throwable, Unit>()
    this.executeTransactionAsync(
            { realm ->
                try {
                    transaction(realm)
                } catch (e: Throwable) {
                    deferred.rejectSafely(e)
                }
            },
            {
                if (deferred.isPending) {
                    deferred.resolve(Unit)
                }
            },
            { error ->
                logException(error)
                deferred.rejectSafely(error)
            })
    return deferred.promise()
}

fun Realm.commitAsyncRx(transaction: (realm: Realm) -> Unit): Completable =
        Completable.create { emitter ->
            this.executeTransactionAsync(
                    { realm ->
                        try {
                            transaction(realm)
                        } catch (e: Throwable) {
                            emitter.onError(e) // TODO: Check if not already emitted
                        }
                    },
                    {
                        emitter.onComplete() // TODO: Check if not already emitted
                    },
                    { error ->
                        logException(error)
                        emitter.onError(error) // TODO: Check if not already emitted
                    })
        }

/**
 * Creates a copy of the RealmObject that is no longer attached to the (persisted!) Realm, i.e.,
 * the returned object is unmanaged and not live-updating.
 *
 * @return The detached copy, or the object itself if it is already unmanaged
</T> */
fun <T : RealmObject> T.detachFromRealm(): T = realm?.copyFromRealm(this) ?: this

/**
 * Creates a copy of the list that is no longer attached to the (persisted!) Realm, i.e.,
 * the returned list contains only unmanaged objects.
 *
 * @return The detached copy, or the list itself if it is empty or its elements are already unmanaged
</T> */
fun <T : RealmObject> List<T>.detachFromRealm(): List<T> = firstOrNull()?.realm?.copyFromRealm(this)
        ?: this
