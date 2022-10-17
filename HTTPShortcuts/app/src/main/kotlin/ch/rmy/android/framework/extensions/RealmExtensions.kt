package ch.rmy.android.framework.extensions

import io.realm.RealmList
import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.kotlin.deleteFromRealm

/**
 * Creates a copy of the RealmModel that is no longer attached to the (persisted!) Realm, i.e.,
 * the returned object is unmanaged and not live-updating.
 *
 * @return The detached copy, or the object itself if it is already unmanaged
</T> */
fun <T : RealmModel> T.detachFromRealm(): T =
    RealmObject.getRealm(this)?.copyFromRealm(this) ?: this

fun <T : RealmModel> RealmResults<T>.detachFromRealm(): List<T> =
    realm?.copyFromRealm(this) ?: this

fun <T : RealmModel, ID : Any> RealmList<T>.swap(id1: ID, id2: ID, getId: T.() -> ID) {
    val oldPosition = indexOfFirstOrNull { it.getId() == id1 } ?: return
    val newPosition = indexOfFirstOrNull { it.getId() == id2 } ?: return
    move(oldPosition, newPosition)
}

fun <T : RealmModel> List<T>.deleteAllFromRealm() {
    forEach {
        it.deleteFromRealm()
    }
}
