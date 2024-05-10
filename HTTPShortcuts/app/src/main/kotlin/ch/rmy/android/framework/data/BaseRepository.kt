package ch.rmy.android.framework.data

import io.realm.kotlin.query.RealmQuery
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

abstract class BaseRepository(private val realmFactory: RealmFactory) {

    protected suspend fun <T : RealmObject> query(query: RealmContext.() -> RealmQuery<T>): List<T> =
        withContext(Dispatchers.IO) {
            realmFactory.getRealmContext()
                .query()
                .find()
        }

    protected suspend fun <T : RealmObject> queryItem(query: RealmContext.() -> RealmQuery<T>): T =
        query(query).first()

    protected fun <T : RealmObject> observeQuery(query: RealmContext.() -> RealmQuery<T>): Flow<List<T>> =
        realmFactory.getRealmContext()
            .query()
            .asFlow()
            .map {
                it.list
            }

    protected fun <T : RealmObject> observeList(query: RealmContext.() -> RealmList<T>): Flow<List<T>> =
        realmFactory.getRealmContext()
            .query()
            .asFlow()
            .map {
                it.list
            }

    protected fun <T : RealmObject> observeItem(query: RealmContext.() -> RealmQuery<T>): Flow<T> =
        observeQuery(query)
            .filter { it.isNotEmpty() }
            .map { it.first() }

    protected suspend fun commitTransaction(transaction: RealmTransactionContext.() -> Unit) {
        realmFactory.updateRealm(transaction)
    }
}
