package ch.rmy.android.framework.data

import android.os.Looper
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.realm.Realm
import io.realm.RealmObject

abstract class RealmObservable<T : RealmObject>(
    private val realmFactory: RealmFactory,
) : Observable<List<T>>() {

    override fun subscribeActual(observer: Observer<in List<T>>) {
        var isDisposed = false
        var realm: Realm? = null
        var looper: Looper? = null
        observer.onSubscribe(object : Disposable {
            override fun dispose() {
                onDispose()
                isDisposed = true
                AndroidSchedulers.from(looper ?: return).scheduleDirect {
                    realm?.close()
                }
            }

            override fun isDisposed() =
                isDisposed
        })

        looper = Looper.myLooper()
        realm = realmFactory.createRealm()
            .apply {
                registerChangeListener(createContext(), observer::onNext)
            }
    }

    protected abstract fun registerChangeListener(realmContext: RealmContext, onDataChanged: (List<T>) -> Unit)

    protected abstract fun onDispose()
}
