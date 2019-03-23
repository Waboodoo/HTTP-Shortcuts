package ch.rmy.android.http_shortcuts.activities.editor

import android.app.Application
import androidx.lifecycle.LiveData
import ch.rmy.android.http_shortcuts.icons.Icons
import ch.rmy.android.http_shortcuts.realm.RealmViewModel
import ch.rmy.android.http_shortcuts.realm.Repository
import ch.rmy.android.http_shortcuts.realm.commitAsync
import ch.rmy.android.http_shortcuts.realm.models.HasId.Companion.FIELD_ID
import ch.rmy.android.http_shortcuts.realm.models.Shortcut
import ch.rmy.android.http_shortcuts.realm.models.Shortcut.Companion.TEMPORARY_ID
import ch.rmy.android.http_shortcuts.realm.toLiveData
import ch.rmy.android.http_shortcuts.utils.UUIDUtils.newUUID
import io.reactivex.Completable
import io.reactivex.Single
import io.realm.Realm
import io.realm.kotlin.where

class ShortcutEditorViewModel(application: Application) : RealmViewModel(application) {

    fun init(categoryId: String?, shortcutId: String?): Completable {
        this.categoryId = categoryId
        this.shortcutId = shortcutId
        return persistedRealm.commitAsync { realm ->
            Repository.deleteShortcut(realm, TEMPORARY_ID)
            if (shortcutId == null) {
                realm.copyToRealmOrUpdate(Shortcut.createNew(id = TEMPORARY_ID, iconName = randomInitialIconName))
            } else {
                Repository.copyShortcut(realm, Repository.getShortcutById(realm, shortcutId)!!, TEMPORARY_ID)
            }
        }
            .doOnComplete {
                isInitialized = true
            }
    }

    var isInitialized: Boolean = false
        private set

    private var categoryId: String? = null
    private var shortcutId: String? = null

    val shortcut: LiveData<Shortcut?>
        get() = persistedRealm
            .where<Shortcut>()
            .equalTo(FIELD_ID, TEMPORARY_ID)
            .findFirstAsync()
            .toLiveData()

    private val randomInitialIconName by lazy {
        Icons.getRandomIcon(application)
    }

    fun hasChanges(): Boolean {
        val oldShortcut = shortcutId
            ?.let { Repository.getShortcutById(persistedRealm, it)!! }
            ?: Shortcut.createNew(iconName = randomInitialIconName)
        val newShortcut = getShortcut(persistedRealm)
        return !newShortcut.isSameAs(oldShortcut)
    }

    fun setNameAndDescription(name: String, description: String): Completable =
        persistedRealm.commitAsync { realm ->
            getShortcut(realm).apply {
                this.name = name
                this.description = description
            }
        }

    fun setIconName(iconName: String?): Completable =
        persistedRealm.commitAsync { realm ->
            getShortcut(realm).apply {
                this.iconName = iconName
            }
        }

    private fun getShortcut(realm: Realm): Shortcut =
        Repository.getShortcutById(realm, TEMPORARY_ID)!!

    fun trySave(): Single<String> {
        // TODO: Validate, abort if invalid

        val id = shortcutId ?: newUUID()
        return persistedRealm.commitAsync { realm ->
            val shortcut = Repository.getShortcutById(realm, TEMPORARY_ID)!!
            val newShortcut = Repository.copyShortcut(realm, shortcut, id)
            if (shortcutId == null && categoryId != null) {
                Repository.getCategoryById(realm, categoryId!!)
                    ?.shortcuts
                    ?.add(newShortcut)
            }

            Repository.deleteShortcut(realm, TEMPORARY_ID)
        }
            .andThen(Single.just(id))
    }

}