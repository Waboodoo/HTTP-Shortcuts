package ch.rmy.android.http_shortcuts.data.domains.categories

import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.domains.BaseRepository
import ch.rmy.android.http_shortcuts.data.enums.CategoryBackgroundType
import ch.rmy.android.http_shortcuts.data.enums.CategoryLayoutType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutClickBehavior
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull

class CategoryRepository
@Inject
constructor(
    database: Database,
) : BaseRepository(database) {

    suspend fun getCategories(): List<Category> = query {
        categoryDao().getCategories()
    }

    suspend fun getCategoryIds(): List<CategoryId> = query {
        categoryDao().getCategoryIds()
    }

    suspend fun getCategoryById(categoryId: CategoryId): Category = query {
        categoryDao().getCategoryById(categoryId).first()
    }

    fun observeCategories(): Flow<List<Category>> = queryFlow {
        categoryDao().observeCategories()
    }

    suspend fun getCategoryByNameOrId(categoryNameOrId: String): Category = query {
        categoryDao().getCategoryByNameOrId(categoryNameOrId).first()
    }

    fun observeCategory(categoryId: CategoryId): Flow<Category> =
        queryFlow {
            categoryDao()
                .observeCategory(categoryId)
                .filterNotNull()
        }

    suspend fun createCategory(
        name: String,
        layoutType: CategoryLayoutType,
        background: CategoryBackgroundType,
        clickBehavior: ShortcutClickBehavior?,
        scale: Float,
    ) {
        commitTransaction {
            val categoryDao = categoryDao()
            val category = Category(
                id = newUUID(),
                name = name,
                layoutType = layoutType,
                background = background,
                shortcutClickBehavior = clickBehavior,
                icon = null,
                hidden = false,
                scale = scale,
                sortingOrder = categoryDao.getCategoryCount(),
            )
            categoryDao.insertOrUpdateCategory(category)
        }
    }

    suspend fun deleteCategory(categoryId: CategoryId) = commitTransaction {
        val categoryDao = categoryDao()
        val category = categoryDao.getCategoryById(categoryId).firstOrNull()
            ?: return@commitTransaction
        categoryDao.deleteCategory(categoryId)
        categoryDao.updateSortingOrder(
            from = category.sortingOrder,
            until = Int.MAX_VALUE,
            diff = -1,
        )
        val shortcutIds = shortcutDao().getShortcutIdsByCategoryId(categoryId)
        shortcutDao().deleteShortcutsByCategoryId(categoryId)
        sectionDao().deleteSectionsByCategoryId(categoryId)
        requestHeaderDao().deleteRequestHeadersByShortcutIds(shortcutIds)
        requestParameterDao().deleteRequestParametersByShortcutIds(shortcutIds)
    }

    suspend fun updateCategory(
        categoryId: CategoryId,
        name: String,
        layoutType: CategoryLayoutType,
        background: CategoryBackgroundType,
        clickBehavior: ShortcutClickBehavior?,
        scale: Float,
    ) {
        commitTransactionForCategory(categoryId) { category ->
            categoryDao().insertOrUpdateCategory(
                category.copy(
                    name = name,
                    layoutType = layoutType,
                    background = background,
                    shortcutClickBehavior = clickBehavior,
                    scale = scale,
                ),
            )
        }
    }

    suspend fun setCategoryHidden(categoryId: CategoryId, hidden: Boolean) {
        commitTransaction {
            val categoryDao = categoryDao()
            if (hidden) {
                val categories = categoryDao.getCategories()
                if (categories.all { it.hidden || it.id == categoryId }) {
                    // Disallow hiding the last non-hidden category
                    return@commitTransaction
                }
            }

            val category = categoryDao.getCategoryById(categoryId)
                .firstOrNull()
                ?: return@commitTransaction
            categoryDao.insertOrUpdateCategory(
                category.copy(
                    hidden = hidden,
                ),
            )
        }
    }

    suspend fun moveCategory(categoryId1: CategoryId, categoryId2: CategoryId) = commitTransaction {
        val categoryDao = categoryDao()
        val category1 = categoryDao.getCategoryById(categoryId1).firstOrNull() ?: return@commitTransaction
        val category2 = categoryDao.getCategoryById(categoryId2).firstOrNull() ?: return@commitTransaction
        if (category1.sortingOrder < category2.sortingOrder) {
            categoryDao.updateSortingOrder(
                from = category1.sortingOrder + 1,
                until = category2.sortingOrder,
                diff = -1,
            )
        } else {
            categoryDao.updateSortingOrder(
                from = category2.sortingOrder,
                until = category1.sortingOrder - 1,
                diff = 1,
            )
        }
        categoryDao.insertOrUpdateCategory(category1.copy(sortingOrder = category2.sortingOrder))
    }

    suspend fun setCategoryIcon(categoryId: CategoryId, icon: ShortcutIcon) {
        commitTransactionForCategory(categoryId) { category ->
            categoryDao().insertOrUpdateCategory(
                category.copy(
                    icon = icon,
                ),
            )
        }
    }

    private suspend fun commitTransactionForCategory(categoryId: CategoryId, transaction: suspend Database.(Category) -> Unit) {
        commitTransaction {
            transaction(
                categoryDao().getCategoryById(categoryId)
                    .firstOrNull()
                    ?: return@commitTransaction,
            )
        }
    }
}
