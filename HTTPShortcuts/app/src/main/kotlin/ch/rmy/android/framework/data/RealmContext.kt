package ch.rmy.android.framework.data

import io.realm.ImportFlag
import io.realm.Realm
import io.realm.RealmModel

interface RealmContext {
    val realmInstance: Realm
}

fun Realm.createContext() =
    object : RealmContext {
        override val realmInstance: Realm
            get() = this@createContext
    }

fun Realm.createTransactionContext() =
    object : RealmTransactionContext {
        override val realmInstance: Realm
            get() = this@createTransactionContext
    }

interface RealmTransactionContext : RealmContext {
    fun <T : RealmModel> copy(`object`: T): T =
        realmInstance.copyToRealm(`object`, ImportFlag.CHECK_SAME_VALUES_BEFORE_SET)

    fun <T : RealmModel> copyOrUpdate(`object`: T): T =
        realmInstance.copyToRealmOrUpdate(`object`, ImportFlag.CHECK_SAME_VALUES_BEFORE_SET)

    fun <T : RealmModel> copyOrUpdate(objects: Iterable<T>): List<T> =
        realmInstance.copyToRealmOrUpdate(objects, ImportFlag.CHECK_SAME_VALUES_BEFORE_SET)
}
