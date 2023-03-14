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
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import io.realm.kotlin.deleteFromRealm
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CategoryRepository
@Inject
constructor(
    realmFactory: RealmFactory,
) : BaseRepository(realmFactory) {

    suspend fun getCategories(): List<Category> =
        queryItem {
            getBase()
        }
            .categories

    fun getObservableCategories(): Flow<List<Category>> =
        observeList {
            getBase().findFirst()!!.categories
        }

    suspend fun getCategory(categoryId: CategoryId): Category =
        queryItem {
            getCategoryById(categoryId)
        }

    fun getObservableCategory(categoryId: CategoryId): Flow<Category> =
        observeItem {
            getCategoryById(categoryId)
        }

    suspend fun createCategory(
        name: String,
        layoutType: CategoryLayoutType,
        background: CategoryBackgroundType,
        clickBehavior: ShortcutClickBehavior?,
    ) {
        commitTransaction {
            val base = getBase()
                .findFirst()
                ?: return@commitTransaction
            val categories = base.categories
            val category = Category(
                name = name,
                categoryLayoutType = layoutType,
                categoryBackgroundType = background,
                clickBehavior = clickBehavior,
            )
            category.id = newUUID()
            categories.add(copy(category))
        }
    }

    suspend fun deleteCategory(categoryId: CategoryId) {
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
    }

    suspend fun updateCategory(
        categoryId: CategoryId,
        name: String,
        layoutType: CategoryLayoutType,
        background: CategoryBackgroundType,
        clickBehavior: ShortcutClickBehavior?,
    ) {
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
    }

    suspend fun toggleCategoryHidden(categoryId: CategoryId, hidden: Boolean) {
        commitTransaction {
            getCategoryById(categoryId)
                .findFirst()
                ?.hidden = hidden
        }
    }

    suspend fun moveCategory(categoryId1: CategoryId, categoryId2: CategoryId) {
        commitTransaction {
            getBase().findFirst()
                ?.categories
                ?.swap(categoryId1, categoryId2) { id }
        }
    }

    suspend fun setCategoryIcon(categoryId: CategoryId, icon: ShortcutIcon) {
        commitTransaction {
            getCategoryById(categoryId)
                .findFirst()
                ?.icon = icon
        }
    }
}
