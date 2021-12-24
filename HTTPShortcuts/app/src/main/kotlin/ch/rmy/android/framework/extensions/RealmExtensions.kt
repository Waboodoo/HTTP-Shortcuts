package ch.rmy.android.framework.extensions

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.RealmResults

/**
 * Creates a copy of the RealmObject that is no longer attached to the (persisted!) Realm, i.e.,
 * the returned object is unmanaged and not live-updating.
 *
 * @return The detached copy, or the object itself if it is already unmanaged
</T> */
fun <T : RealmObject> T.detachFromRealm(): T =
    realm?.copyFromRealm(this) ?: this

fun <T : RealmObject> RealmResults<T>.detachFromRealm(): List<T> =
    realm?.copyFromRealm(this) ?: this

fun <T : RealmObject> RealmList<T>.detachFromRealm(): List<T> =
    realm?.copyFromRealm(this) ?: this
