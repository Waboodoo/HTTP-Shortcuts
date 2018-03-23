package ch.rmy.android.http_shortcuts.realm

import ch.rmy.android.http_shortcuts.utils.logException
import io.realm.Realm
import org.jdeferred.Promise
import org.jdeferred.impl.DeferredObject

fun Realm.commitAsync(transaction: (realm: Realm) -> Unit): Promise<Unit, Throwable, Unit> {
    val deferred = DeferredObject<Unit, Throwable, Unit>()
    this.executeTransactionAsync(
            object : Realm.Transaction {
                override fun execute(realm: Realm) {
                    try {
                        transaction(realm)
                    } catch (e: Throwable) {
                        deferred.reject(e)
                    }
                }
            },
            Realm.Transaction.OnSuccess {
                if (deferred.isPending) {
                    deferred.resolve(Unit)
                }
            },
            Realm.Transaction.OnError { error ->
                logException(error)
                deferred.reject(error)
            })
    return deferred.promise()
}