package ch.rmy.android.http_shortcuts.activities.main

import android.app.Application
import androidx.lifecycle.LiveData
import ch.rmy.android.http_shortcuts.data.Repository
import ch.rmy.android.http_shortcuts.data.Repository.copyShortcut
import ch.rmy.android.http_shortcuts.data.Repository.getBase
import ch.rmy.android.http_shortcuts.data.Repository.getCategoryByIdAsync
import ch.rmy.android.http_shortcuts.data.Repository.getPendingExecutions
import ch.rmy.android.http_shortcuts.data.Repository.getShortcutById
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

    var exportedShortcutId: String? = null

    fun getCategory(): LiveData<Category?> =
        getCategoryByIdAsync(persistedRealm, categoryId)
            .toLiveData()

    fun getPendingShortcuts(): ListLiveData<PendingExecution> =
        getPendingExecutions(persistedRealm)
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
            getPendingExecutions(realm, shortcutId).deleteAllFromRealm()
            shortcut.headers.deleteAllFromRealm()
            shortcut.parameters.deleteAllFromRealm()
            shortcut.deleteFromRealm()
            Repository.getDeadWidgets(realm).forEach { widget ->
                widget.deleteFromRealm()
            }
        }

    fun removePendingExecution(shortcutId: String) =
        Transactions.commit { realm ->
            getPendingExecutions(realm, shortcutId).deleteAllFromRealm()
        }

    fun duplicateShortcut(shortcutId: String, newName: String, newPosition: Int?, categoryId: String) =
        Transactions.commit { realm ->
            val shortcut = getShortcutById(realm, shortcutId) ?: return@commit
            val newShortcut = copyShortcut(realm, shortcut, newUUID())
            newShortcut.name = newName
            moveShortcut(realm, newShortcut.id, newPosition, categoryId)
        }

}