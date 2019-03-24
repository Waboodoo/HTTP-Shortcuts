package ch.rmy.android.http_shortcuts.activities.categories

import android.app.Application
import ch.rmy.android.http_shortcuts.realm.RealmViewModel
import ch.rmy.android.http_shortcuts.realm.Repository
import ch.rmy.android.http_shortcuts.realm.commitAsync
import ch.rmy.android.http_shortcuts.realm.livedata.ListLiveData
import ch.rmy.android.http_shortcuts.realm.models.Category
import ch.rmy.android.http_shortcuts.realm.toLiveData
import ch.rmy.android.http_shortcuts.utils.UUIDUtils.newUUID

class CategoriesViewModel(application: Application) : RealmViewModel(application) {

    fun getCategories(): ListLiveData<Category> =
        Repository.getBase(persistedRealm)!!
            .categories
            .toLiveData()

    fun createCategory(name: String) =
        persistedRealm.commitAsync { realm ->
            val base = Repository.getBase(realm) ?: return@commitAsync
            val categories = base.categories
            val category = Category.createNew(name)
            category.id = newUUID()
            categories.add(realm.copyToRealm(category))
        }

    fun renameCategory(categoryId: String, newName: String) =
        persistedRealm.commitAsync { realm ->
            Repository.getCategoryById(realm, categoryId)?.name = newName
        }

    fun setLayoutType(categoryId: String, layoutType: String) =
        persistedRealm.commitAsync { realm ->
            Repository.getCategoryById(realm, categoryId)?.layoutType = layoutType
        }

    fun moveCategory(categoryId: String, position: Int) =
        persistedRealm.commitAsync { realm ->
            val base = Repository.getBase(realm) ?: return@commitAsync
            val category = Repository.getCategoryById(realm, categoryId) ?: return@commitAsync
            val categories = base.categories
            val oldPosition = categories.indexOf(category)
            categories.move(oldPosition, position)
        }

    fun deleteCategory(categoryId: String) =
        persistedRealm.commitAsync { realm ->
            val category = Repository.getCategoryById(realm, categoryId) ?: return@commitAsync
            for (shortcut in category.shortcuts) {
                shortcut.headers.deleteAllFromRealm()
                shortcut.parameters.deleteAllFromRealm()
            }
            category.shortcuts.deleteAllFromRealm()
            category.deleteFromRealm()
        }

    fun setBackground(categoryId: String, background: String) =
        persistedRealm.commitAsync { realm ->
            Repository.getCategoryById(realm, categoryId)?.background = background
        }

}