package ch.rmy.android.http_shortcuts.activities.main

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import ch.rmy.android.http_shortcuts.data.RealmViewModel
import ch.rmy.android.http_shortcuts.data.Repository
import ch.rmy.android.http_shortcuts.data.Transactions
import ch.rmy.android.http_shortcuts.data.livedata.ListLiveData
import ch.rmy.android.http_shortcuts.data.models.AppLock
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.extensions.toLiveData
import io.realm.kotlin.where
import org.mindrot.jbcrypt.BCrypt

open class MainViewModel(application: Application) : RealmViewModel(application) {

    fun isAppLocked() = Repository.getAppLock(persistedRealm) != null

    val appLockedSource: LiveData<Boolean>
        get() = Transformations.map(
            persistedRealm
                .where<AppLock>()
                .findAllAsync()
                .toLiveData()
        ) {
            it.isNotEmpty()
        }

    fun removeAppLock(password: String) =
        Transactions.commit { realm ->
            val appLock = Repository.getAppLock(realm)
            if (appLock != null && BCrypt.checkpw(password, appLock.passwordHash)) {
                appLock.deleteFromRealm()
            }
        }

    fun getCategories(): ListLiveData<Category> =
        Repository.getBase(persistedRealm)!!
            .categories
            .toLiveData()

    fun getShortcutById(shortcutId: String) = Repository.getShortcutById(persistedRealm, shortcutId)

    fun moveShortcut(shortcutId: String, targetPosition: Int? = null, targetCategoryId: String? = null) =
        Transactions.commit { realm ->
            Repository.moveShortcut(realm, shortcutId, targetPosition, targetCategoryId)
        }

}