package ch.rmy.android.http_shortcuts.activities.main

import android.app.Application
import androidx.lifecycle.LiveData
import ch.rmy.android.http_shortcuts.data.Repository.copyShortcut
import ch.rmy.android.http_shortcuts.data.Repository.getBase
import ch.rmy.android.http_shortcuts.data.Repository.getCategoryByIdAsync
import ch.rmy.android.http_shortcuts.data.Repository.getShortcutById
import ch.rmy.android.http_shortcuts.data.Repository.getShortcutPendingExecution
import ch.rmy.android.http_shortcuts.data.Repository.getShortcutsPendingExecution
import ch.rmy.android.http_shortcuts.data.Repository.moveShortcut
import ch.rmy.android.http_shortcuts.data.Transactions
import ch.rmy.android.http_shortcuts.data.livedata.ListLiveData
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.PendingExecution
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.toLiveData
import ch.rmy.android.http_shortcuts.utils.UUIDUtils.newUUID

class ShortcutListViewModel(application: Application) : MainViewModel(application) {

    var categoryId: String = ""

    fun getCategory(): LiveData<Category?> =
        getCategoryByIdAsync(persistedRealm, categoryId)
            .toLiveData()

    fun getPendingShortcuts(): ListLiveData<PendingExecution> =
        getShortcutsPendingExecution(persistedRealm)
            .toLiveData()

    fun getShortcuts(): ListLiveData<Shortcut> =
        getBase(persistedRealm)!!
            .categories
            .firstOrNull { category -> category.id == categoryId }
            ?.shortcuts
            ?.toLiveData()
            ?: (object : ListLiveData<Shortcut>() {})

    fun deleteShortcut(shortcutId: String) =
        Transactions.commit { realm ->
            val shortcut = getShortcutById(realm, shortcutId) ?: return@commit
            getShortcutPendingExecution(realm, shortcutId)?.deleteFromRealm()
            shortcut.headers.deleteAllFromRealm()
            shortcut.parameters.deleteAllFromRealm()
            shortcut.deleteFromRealm()
        }

    fun removePendingExecution(shortcutId: String) =
        Transactions.commit { realm ->
            getShortcutPendingExecution(realm, shortcutId)?.deleteFromRealm()
        }

    fun duplicateShortcut(shortcutId: String, newName: String, newPosition: Int?, categoryId: String) =
        Transactions.commit { realm ->
            val shortcut = getShortcutById(realm, shortcutId) ?: return@commit
            val newShortcut = copyShortcut(realm, shortcut, newUUID())
            newShortcut.name = newName
            moveShortcut(realm, newShortcut.id, newPosition, categoryId)
        }

}