package ch.rmy.android.http_shortcuts.extensions

import androidx.lifecycle.LiveData
import ch.rmy.android.http_shortcuts.data.livedata.ListLiveData
import ch.rmy.android.http_shortcuts.data.livedata.RealmListLiveData
import ch.rmy.android.http_shortcuts.data.livedata.RealmResultsLiveData
import ch.rmy.android.http_shortcuts.data.livedata.RealmSingleLiveData
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.RealmResults

/**
 * Creates a copy of the RealmObject that is no longer attached to the (persisted!) Realm, i.e.,
 * the returned object is unmanaged and not live-updating.
 *
 * @return The detached copy, or the object itself if it is already unmanaged
</T> */
fun <T : RealmObject> T.detachFromRealm(): T = realm?.copyFromRealm(this) ?: this

fun <T : RealmObject> RealmList<T>.detachFromRealm(): List<T> = realm?.copyFromRealm(this) ?: this

fun <T : RealmObject> T.toLiveData(): LiveData<T?> = RealmSingleLiveData(this)

fun <T : RealmObject> RealmResults<T>.toLiveData(): ListLiveData<T> = RealmResultsLiveData(this)

fun <T : RealmObject> RealmList<T>.toLiveData(): ListLiveData<T> = RealmListLiveData(this)
