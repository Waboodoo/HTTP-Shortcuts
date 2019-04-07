package ch.rmy.android.http_shortcuts.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import io.realm.Realm

abstract class RealmViewModel(application: Application) : AndroidViewModel(application) {

    protected val persistedRealm: Realm = RealmFactory.getInstance().createRealm()

    override fun onCleared() {
        persistedRealm.close()
    }
}