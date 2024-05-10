package ch.rmy.android.framework.data

import io.realm.kotlin.MutableRealm
import io.realm.kotlin.TypedRealm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.query.RealmQuery
import io.realm.kotlin.query.TRUE_PREDICATE
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.TypedRealmObject
import kotlin.reflect.KClass

open class RealmContext(private val realmInstance: TypedRealm) {

    inline fun <reified T : RealmObject> get(query: String = TRUE_PREDICATE, vararg args: Any?): RealmQuery<T> =
        get(T::class, query, *args)

    fun <T : RealmObject> get(clazz: KClass<T>, query: String = TRUE_PREDICATE, vararg args: Any?): RealmQuery<T> =
        realmInstance.query(clazz, query, *args)

    fun <T : RealmObject> RealmQuery<T>.findFirst(): T? =
        first().find()
}

class RealmTransactionContext(private val realmInstance: MutableRealm) : RealmContext(realmInstance) {

    fun <T : RealmObject> copy(`object`: T): T =
        realmInstance.copyToRealm(`object`, UpdatePolicy.ERROR)

    fun <T : RealmObject> copyOrUpdate(`object`: T): T =
        realmInstance.copyToRealm(`object`, UpdatePolicy.ALL)

    fun <T : RealmObject> copyOrUpdate(objects: Iterable<T>?): List<T> =
        objects
            ?.map {
                realmInstance.copyToRealm(it, UpdatePolicy.ALL)
            }
            ?: emptyList()

    fun <T : TypedRealmObject> T.delete() {
        realmInstance.delete(this)
    }

    fun <T : TypedRealmObject> List<T>.deleteAll() {
        reversed()
            .forEach {
                it.delete()
            }
    }

    fun <T : TypedRealmObject> RealmQuery<T>.deleteAll() {
        find().deleteAll()
    }
}
