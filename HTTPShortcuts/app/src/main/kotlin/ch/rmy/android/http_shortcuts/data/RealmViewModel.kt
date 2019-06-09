package ch.rmy.android.http_shortcuts.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import io.realm.Realm

abstract class RealmViewModel(application: Application) : AndroidViewModel(application) {

    protected val persistedRealm: Realm
        get() {
            if (realm == null) {
                realm = RealmFactory.getInstance().createRealm()
            }
            return realm!!
        }

    private var realm: Realm? = null

    override fun onCleared() {
        realm?.close()
    }
}