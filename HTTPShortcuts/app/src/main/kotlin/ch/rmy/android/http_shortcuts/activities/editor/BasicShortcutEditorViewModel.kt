package ch.rmy.android.http_shortcuts.activities.editor

import android.app.Application
import androidx.lifecycle.LiveData
import ch.rmy.android.http_shortcuts.data.RealmViewModel
import ch.rmy.android.http_shortcuts.data.Repository
import ch.rmy.android.http_shortcuts.data.livedata.ListLiveData
import ch.rmy.android.http_shortcuts.data.models.Base
import ch.rmy.android.http_shortcuts.data.models.HasId
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.extensions.toLiveData
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.kotlin.where

abstract class BasicShortcutEditorViewModel(application: Application) : RealmViewModel(application) {

    val shortcut: LiveData<Shortcut?> by lazy {
        persistedRealm
            .where<Shortcut>()
            .equalTo(HasId.FIELD_ID, Shortcut.TEMPORARY_ID)
            .findFirstAsync()
            .toLiveData()
    }

    val shortcuts: ListLiveData<Shortcut> = object : ListLiveData<Shortcut>() {

        private val base: Base = Repository.getBaseAsync(persistedRealm)

        private val changeListener = RealmChangeListener<Base> { onChange() }

        override fun onActive() {
            base.addChangeListener(changeListener)
        }

        override fun onInactive() {
            base.removeChangeListener(changeListener)
        }

        override fun getValue(): List<Shortcut>? =
            base
                .takeIf { it.isLoaded && it.isValid }
                ?.categories
                ?.flatMap {
                    it.shortcuts
                }
    }

    val variables: ListLiveData<Variable> by lazy {
        Repository.getBase(persistedRealm)!!
            .variables
            .toLiveData()
    }

    protected fun getShortcut(realm: Realm): Shortcut? =
        Repository.getShortcutById(realm, Shortcut.TEMPORARY_ID)

}