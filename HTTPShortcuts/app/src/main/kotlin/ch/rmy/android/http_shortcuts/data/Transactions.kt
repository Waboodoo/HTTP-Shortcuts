package ch.rmy.android.http_shortcuts.data

import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.Realm

object Transactions {

    fun commit(transaction: (Realm) -> Unit): Completable =
        Completable.fromAction {
            RealmFactory.getInstance().createRealm().use { realm ->
                realm.executeTransaction(transaction)
            }
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

}