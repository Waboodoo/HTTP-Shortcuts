package ch.rmy.android.http_shortcuts.activities.editor

import android.app.Application
import androidx.lifecycle.LiveData
import ch.rmy.android.http_shortcuts.data.RealmViewModel
import ch.rmy.android.http_shortcuts.data.Repository
import ch.rmy.android.http_shortcuts.data.livedata.ListLiveData
import ch.rmy.android.http_shortcuts.data.models.HasId
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.extensions.toLiveData
import io.realm.Realm
import io.realm.kotlin.where

abstract class BasicShortcutEditorViewModel(application: Application) : RealmViewModel(application) {

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

    protected fun getShortcut(realm: Realm): Shortcut? =
        Repository.getShortcutById(realm, Shortcut.TEMPORARY_ID)

}