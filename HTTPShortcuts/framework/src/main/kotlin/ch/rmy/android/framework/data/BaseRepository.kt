package ch.rmy.android.framework.data

import ch.rmy.android.framework.extensions.detachFromRealm
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmModel
import io.realm.RealmQuery
import io.realm.kotlin.toFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

abstract class BaseRepository(private val realmFactory: RealmFactory) {

    protected suspend fun <T : RealmModel> query(query: RealmContext.() -> RealmQuery<T>): List<T> =
        withContext(Dispatchers.IO) {
            realmFactory.createRealm().use { realm ->
                query(realm.createContext())
                    .findAll()
                    .detachFromRealm()
            }
        }

    protected suspend fun <T : RealmModel> queryItem(query: RealmContext.() -> RealmQuery<T>): T =
        query(query).first()

    protected fun <T : RealmModel> observeQuery(query: RealmContext.() -> RealmQuery<T>): Flow<List<T>> =
        channelFlow {
            withRealmContext(realmFactory) {
                query()
                    .findAllAsync()
                    .toFlow()
                    .collect(channel::send)
            }
        }

    protected fun <T : RealmModel> observeList(query: RealmContext.() -> RealmList<T>): Flow<List<T>> =
        channelFlow {
            withRealmContext(realmFactory) {
                query()
                    .toFlow()
                    .collect(channel::send)
            }
        }

    private suspend fun ProducerScope<*>.withRealmContext(realmFactory: RealmFactory, block: suspend RealmContext.() -> Unit) {
        var realm: Realm
        withContext(Dispatchers.Main) { // TODO: Check if this could be done on a different thread (which has a looper)
            realm = realmFactory.createRealm()
            realm.createContext().block()
        }
        awaitClose(realm::close)
    }

    protected fun <T : RealmModel> observeItem(query: RealmContext.() -> RealmQuery<T>): Flow<T> =
        observeQuery(query)
            .filter { it.isNotEmpty() }
            .map { it.first() }

    protected suspend fun commitTransaction(transaction: RealmTransactionContext.() -> Unit) {
        withContext(Dispatchers.IO) {
            realmFactory.createRealm().use { realm ->
                realm.executeTransaction {
                    transaction(realm.createTransactionContext())
                }
            }
        }
    }
}
