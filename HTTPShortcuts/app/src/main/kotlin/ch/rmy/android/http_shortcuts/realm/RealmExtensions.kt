package ch.rmy.android.http_shortcuts.realm

import androidx.lifecycle.LiveData
import ch.rmy.android.http_shortcuts.extensions.logException
import ch.rmy.android.http_shortcuts.realm.livedata.ListLiveData
import ch.rmy.android.http_shortcuts.realm.livedata.RealmListLiveData
import ch.rmy.android.http_shortcuts.realm.livedata.RealmResultsLiveData
import ch.rmy.android.http_shortcuts.realm.livedata.RealmSingleLiveData
import io.reactivex.Completable
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.RealmResults

fun Realm.commitAsync(transaction: (realm: Realm) -> Unit): Completable =
    Completable.create { emitter ->
        this.executeTransactionAsync(
            { realm ->
                try {
                    transaction(realm)
                } catch (e: Throwable) {
                    emitter.onError(e)
                }
            },
            {
                emitter.onComplete()
            },
            { error ->
                logException(error)
                emitter.onError(error)
            })
    }

/**
 * Creates a copy of the RealmObject that is no longer attached to the (persisted!) Realm, i.e.,
 * the returned object is unmanaged and not live-updating.
 *
 * @return The detached copy, or the object itself if it is already unmanaged
</T> */
fun <T : RealmObject> T.detachFromRealm(): T = realm?.copyFromRealm(this) ?: this

fun <T : RealmObject> T.toLiveData(): LiveData<T?> = RealmSingleLiveData(this)

fun <T : RealmObject> RealmResults<T>.toLiveData(): ListLiveData<T> = RealmResultsLiveData(this)

fun <T : RealmObject> RealmList<T>.toLiveData(): ListLiveData<T> = RealmListLiveData(this)
