package ch.rmy.android.http_shortcuts.data.domains.categories

import ch.rmy.android.framework.data.BaseRepository
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.data.RealmFactory
import ch.rmy.android.http_shortcuts.data.domains.getBase
import ch.rmy.android.http_shortcuts.data.domains.getCategoryById
import ch.rmy.android.http_shortcuts.data.enums.CategoryBackgroundType
import ch.rmy.android.http_shortcuts.data.enums.CategoryLayoutType
import ch.rmy.android.http_shortcuts.data.models.Category
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class CategoryRepository : BaseRepository(RealmFactory.getInstance()) {

    fun getCategories(): Single<List<Category>> =
        queryItem {
            getBase()
        }
            .map { base ->
                base.categories
            }

    fun getObservableCategories(): Observable<List<Category>> =
        observeList {
            getBase().findFirst()!!.categories
        }

    fun getObservableCategory(categoryId: String): Observable<Category> =
        observeItem {
            getCategoryById(categoryId)
        }

    fun createCategory(name: String): Completable =
        commitTransaction {
            val base = getBase()
                .findFirst()
                ?: return@commitTransaction
            val categories = base.categories
            val category = Category(name)
            category.id = newUUID()
            categories.add(copy(category))
        }

    fun deleteCategory(categoryId: String): Completable =
        commitTransaction {
            val category = getCategoryById(categoryId)
                .findFirst()
                ?: return@commitTransaction
            for (shortcut in category.shortcuts) {
                shortcut.headers.deleteAllFromRealm()
                shortcut.parameters.deleteAllFromRealm()
            }
            category.shortcuts.deleteAllFromRealm()
            category.deleteFromRealm()
        }

    fun setBackground(categoryId: String, background: CategoryBackgroundType): Completable =
        commitTransaction {
            getCategoryById(categoryId)
                .findFirst()
                ?.categoryBackgroundType = background
        }

    fun renameCategory(categoryId: String, newName: String): Completable =
        commitTransaction {
            getCategoryById(categoryId)
                .findFirst()
                ?.name = newName
        }

    fun toggleCategoryHidden(categoryId: String, hidden: Boolean): Completable =
        commitTransaction {
            getCategoryById(categoryId)
                .findFirst()
                ?.hidden = hidden
        }

    fun setLayoutType(categoryId: String, layoutType: CategoryLayoutType): Completable =
        commitTransaction {
            getCategoryById(categoryId)
                .findFirst()
                ?.categoryLayoutType = layoutType
        }

    fun moveCategory(categoryId: String, position: Int): Completable =
        commitTransaction {
            val base = getBase().findFirst() ?: return@commitTransaction
            val category = getCategoryById(categoryId).findFirst() ?: return@commitTransaction
            val categories = base.categories
            val oldPosition = categories.indexOf(category)
            categories.move(oldPosition, position)
        }
}
