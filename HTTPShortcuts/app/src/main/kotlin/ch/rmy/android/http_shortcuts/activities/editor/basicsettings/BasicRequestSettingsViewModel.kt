package ch.rmy.android.http_shortcuts.activities.editor.basicsettings

import android.app.Application
import androidx.lifecycle.LiveData
import ch.rmy.android.http_shortcuts.realm.RealmViewModel
import ch.rmy.android.http_shortcuts.realm.Repository
import ch.rmy.android.http_shortcuts.realm.commitAsync
import ch.rmy.android.http_shortcuts.realm.livedata.ListLiveData
import ch.rmy.android.http_shortcuts.realm.models.HasId
import ch.rmy.android.http_shortcuts.realm.models.Shortcut
import ch.rmy.android.http_shortcuts.realm.models.Variable
import ch.rmy.android.http_shortcuts.realm.toLiveData
import io.reactivex.Completable
import io.realm.Realm
import io.realm.kotlin.where

class BasicRequestSettingsViewModel(application: Application) : RealmViewModel(application) {

    val shortcut: LiveData<Shortcut?>
        get() = persistedRealm
            .where<Shortcut>()
            .equalTo(HasId.FIELD_ID, Shortcut.TEMPORARY_ID)
            .findFirstAsync()
            .toLiveData()

    val variables: ListLiveData<Variable>
        get() = Repository.getBase(persistedRealm)!!
            .variables
            .toLiveData()

    fun setMethod(method: String): Completable =
        persistedRealm.commitAsync { realm ->
            getShortcut(realm)?.method = method
        }

    fun setUrl(url: String): Completable =
        persistedRealm.commitAsync { realm ->
            getShortcut(realm)?.url = url
        }

    private fun getShortcut(realm: Realm): Shortcut? =
        Repository.getShortcutById(realm, Shortcut.TEMPORARY_ID)

}