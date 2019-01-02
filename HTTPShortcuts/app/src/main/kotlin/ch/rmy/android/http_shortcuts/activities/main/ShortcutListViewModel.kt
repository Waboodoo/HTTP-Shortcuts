package ch.rmy.android.http_shortcuts.activities.main

import android.app.Application
import androidx.lifecycle.LiveData
import ch.rmy.android.http_shortcuts.realm.Repository.copyShortcut
import ch.rmy.android.http_shortcuts.realm.Repository.generateId
import ch.rmy.android.http_shortcuts.realm.Repository.getBase
import ch.rmy.android.http_shortcuts.realm.Repository.getCategoryByIdAsync
import ch.rmy.android.http_shortcuts.realm.Repository.getShortcutById
import ch.rmy.android.http_shortcuts.realm.Repository.getShortcutPendingExecution
import ch.rmy.android.http_shortcuts.realm.Repository.getShortcutsPendingExecution
import ch.rmy.android.http_shortcuts.realm.Repository.moveShortcut
import ch.rmy.android.http_shortcuts.realm.commitAsync
import ch.rmy.android.http_shortcuts.realm.livedata.ListLiveData
import ch.rmy.android.http_shortcuts.realm.models.Category
import ch.rmy.android.http_shortcuts.realm.models.PendingExecution
import ch.rmy.android.http_shortcuts.realm.models.Shortcut
import ch.rmy.android.http_shortcuts.realm.toLiveData

class ShortcutListViewModel(application: Application) : MainViewModel(application) {

    var categoryId: Long = 0

    fun getCategory(): LiveData<Category?> =
        getCategoryByIdAsync(persistedRealm, categoryId)
            .toLiveData()

    fun getPendingShortcuts(): ListLiveData<PendingExecution> =
        getShortcutsPendingExecution(persistedRealm)
            .toLiveData()

    fun getShortcuts(): ListLiveData<Shortcut> =
        getBase(persistedRealm)!!
            .categories
            .first { category -> category.id == categoryId }
            .shortcuts
            .toLiveData()

    fun deleteShortcut(shortcutId: Long) =
        persistedRealm.commitAsync { realm ->
            val shortcut = getShortcutById(realm, shortcutId) ?: return@commitAsync
            getShortcutPendingExecution(realm, shortcutId)?.deleteFromRealm()
            shortcut.headers.deleteAllFromRealm()
            shortcut.parameters.deleteAllFromRealm()
            shortcut.deleteFromRealm()
        }

    fun removePendingExecution(shortcutId: Long) =
        persistedRealm.commitAsync { realm ->
            getShortcutPendingExecution(realm, shortcutId)?.deleteFromRealm()
        }

    fun duplicateShortcut(shortcutId: Long, newName: String, newPosition: Int?, categoryId: Long) =
        persistedRealm.commitAsync { realm ->
            val shortcut = getShortcutById(realm, shortcutId) ?: return@commitAsync
            val newShortcutID = generateId(realm, Shortcut::class.java)
            val newShortcut = copyShortcut(realm, shortcut, newShortcutID)
            newShortcut.name = newName
            moveShortcut(realm, newShortcut.id, newPosition, categoryId)
        }

}