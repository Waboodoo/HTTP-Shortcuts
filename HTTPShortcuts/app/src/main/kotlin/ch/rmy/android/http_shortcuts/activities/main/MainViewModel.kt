package ch.rmy.android.http_shortcuts.activities.main

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import ch.rmy.android.http_shortcuts.realm.RealmViewModel
import ch.rmy.android.http_shortcuts.realm.Repository
import ch.rmy.android.http_shortcuts.realm.commitAsync
import ch.rmy.android.http_shortcuts.realm.livedata.ListLiveData
import ch.rmy.android.http_shortcuts.realm.models.AppLock
import ch.rmy.android.http_shortcuts.realm.models.Category
import ch.rmy.android.http_shortcuts.realm.toLiveData
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
        persistedRealm.commitAsync { realm ->
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
        persistedRealm.commitAsync { realm ->
            Repository.moveShortcut(realm, shortcutId, targetPosition, targetCategoryId)
        }

}