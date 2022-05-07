package ch.rmy.android.http_shortcuts.data.domains.categories

import ch.rmy.android.framework.data.BaseRepository
import ch.rmy.android.framework.data.RealmFactory
import ch.rmy.android.framework.extensions.swap
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.data.domains.getBase
import ch.rmy.android.http_shortcuts.data.domains.getCategoryById
import ch.rmy.android.http_shortcuts.data.enums.CategoryBackgroundType
import ch.rmy.android.http_shortcuts.data.enums.CategoryLayoutType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutClickBehavior
import ch.rmy.android.http_shortcuts.data.models.CategoryModel
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class CategoryRepository
@Inject
constructor(
    realmFactory: RealmFactory,
) : BaseRepository(realmFactory) {

    fun getCategories(): Single<List<CategoryModel>> =
        queryItem {
            getBase()
        }
            .map { base ->
                base.categories
            }

    fun getObservableCategories(): Observable<List<CategoryModel>> =
        observeList {
            getBase().findFirst()!!.categories
        }

    fun getCategory(categoryId: CategoryId): Single<CategoryModel> =
        queryItem {
            getCategoryById(categoryId)
        }

    fun getObservableCategory(categoryId: CategoryId): Observable<CategoryModel> =
        observeItem {
            getCategoryById(categoryId)
        }

    fun createCategory(
        name: String,
        layoutType: CategoryLayoutType,
        background: CategoryBackgroundType,
        clickBehavior: ShortcutClickBehavior?,
    ): Completable =
        commitTransaction {
            val base = getBase()
                .findFirst()
                ?: return@commitTransaction
            val categories = base.categories
            val category = CategoryModel(
                name = name,
                categoryLayoutType = layoutType,
                categoryBackgroundType = background,
                clickBehavior = clickBehavior,
            )
            category.id = newUUID()
            categories.add(copy(category))
        }

    fun deleteCategory(categoryId: CategoryId): Completable =
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

    fun updateCategory(
        categoryId: CategoryId,
        name: String,
        layoutType: CategoryLayoutType,
        background: CategoryBackgroundType,
        clickBehavior: ShortcutClickBehavior?,
    ): Completable =
        commitTransaction {
            getCategoryById(categoryId)
                .findFirst()
                ?.let { category ->
                    category.name = name
                    category.categoryLayoutType = layoutType
                    category.categoryBackgroundType = background
                    category.clickBehavior = clickBehavior
                }
        }

    fun toggleCategoryHidden(categoryId: CategoryId, hidden: Boolean): Completable =
        commitTransaction {
            getCategoryById(categoryId)
                .findFirst()
                ?.hidden = hidden
        }

    fun moveCategory(categoryId1: CategoryId, categoryId2: CategoryId): Completable =
        commitTransaction {
            getBase().findFirst()
                ?.categories
                ?.swap(categoryId1, categoryId2) { id }
        }
}
