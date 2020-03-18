package ch.rmy.android.http_shortcuts.activities.categories

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ch.rmy.android.http_shortcuts.data.RealmViewModel
import ch.rmy.android.http_shortcuts.data.Repository
import ch.rmy.android.http_shortcuts.data.Transactions
import ch.rmy.android.http_shortcuts.data.livedata.ListLiveData
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.extensions.toLiveData
import ch.rmy.android.http_shortcuts.utils.UUIDUtils.newUUID

class CategoriesViewModel(application: Application) : RealmViewModel(application) {

    private val categoriesChanged: MutableLiveData<Boolean> = MutableLiveData<Boolean>()

    val hasChanges: LiveData<Boolean> = categoriesChanged

    fun getCategories(): ListLiveData<Category> =
        Repository.getBase(persistedRealm)!!
            .categories
            .toLiveData()

    fun createCategory(name: String) =
        Transactions
            .commit { realm ->
                val base = Repository.getBase(realm) ?: return@commit
                val categories = base.categories
                val category = Category.createNew(name)
                category.id = newUUID()
                categories.add(realm.copyToRealm(category))
            }
            .doOnComplete {
                categoriesChanged.value = true
            }

    fun renameCategory(categoryId: String, newName: String) =
        Transactions
            .commit { realm ->
                Repository.getCategoryById(realm, categoryId)?.name = newName
            }
            .doOnComplete {
                categoriesChanged.value = true
            }

    fun toggleCategoryHidden(categoryId: String, hidden: Boolean) =
        Transactions
            .commit { realm ->
                Repository.getCategoryById(realm, categoryId)?.hidden = hidden
            }
            .doOnComplete {
                categoriesChanged.value = true
            }

    fun setLayoutType(categoryId: String, layoutType: String) =
        Transactions
            .commit { realm ->
                Repository.getCategoryById(realm, categoryId)?.layoutType = layoutType
            }
            .doOnComplete {
                categoriesChanged.value = true
            }

    fun moveCategory(categoryId: String, position: Int) =
        Transactions
            .commit { realm ->
                val base = Repository.getBase(realm) ?: return@commit
                val category = Repository.getCategoryById(realm, categoryId) ?: return@commit
                val categories = base.categories
                val oldPosition = categories.indexOf(category)
                categories.move(oldPosition, position)
            }
            .doOnComplete {
                categoriesChanged.value = true
            }

    fun deleteCategory(categoryId: String) =
        Transactions
            .commit { realm ->
                val category = Repository.getCategoryById(realm, categoryId) ?: return@commit
                for (shortcut in category.shortcuts) {
                    shortcut.headers.deleteAllFromRealm()
                    shortcut.parameters.deleteAllFromRealm()
                }
                category.shortcuts.deleteAllFromRealm()
                category.deleteFromRealm()
            }
            .doOnComplete {
                categoriesChanged.value = true
            }

    fun setBackground(categoryId: String, background: String) =
        Transactions
            .commit { realm ->
                Repository.getCategoryById(realm, categoryId)?.background = background
            }
            .doOnComplete {
                categoriesChanged.value = true
            }

}