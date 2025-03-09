package ch.rmy.android.http_shortcuts.data.domains.categories

import ch.rmy.android.framework.data.BaseRealmRepository
import ch.rmy.android.framework.data.RealmFactory
import ch.rmy.android.framework.data.RealmTransactionContext
import ch.rmy.android.framework.extensions.swap
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.domains.getBase
import ch.rmy.android.http_shortcuts.data.domains.getCategoryById
import ch.rmy.android.http_shortcuts.data.domains.getCategoryByNameOrId
import ch.rmy.android.http_shortcuts.data.domains.sections.SectionId
import ch.rmy.android.http_shortcuts.data.enums.CategoryBackgroundType
import ch.rmy.android.http_shortcuts.data.enums.CategoryLayoutType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutClickBehavior
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.Section
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class CategoryRepository
@Inject
constructor(
    database: Database,
    realmFactory: RealmFactory,
) : BaseRealmRepository(database, realmFactory) {

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

    suspend fun getCategoryByNameOrId(categoryNameOrId: String): Category =
        queryItem {
            this.getCategoryByNameOrId(categoryNameOrId)
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
        commitTransactionForCategory(categoryId) { category ->
            for (shortcut in category.shortcuts) {
                shortcut.headers.deleteAll()
                shortcut.parameters.deleteAll()
            }
            category.shortcuts.deleteAll()
            category.delete()
        }
    }

    suspend fun updateCategory(
        categoryId: CategoryId,
        name: String,
        layoutType: CategoryLayoutType,
        background: CategoryBackgroundType,
        clickBehavior: ShortcutClickBehavior?,
    ) {
        commitTransactionForCategory(categoryId) { category ->
            category.name = name
            category.categoryLayoutType = layoutType
            category.categoryBackgroundType = background
            category.clickBehavior = clickBehavior
        }
    }

    suspend fun setCategoryHidden(categoryId: CategoryId, hidden: Boolean) {
        commitTransaction {
            if (hidden) {
                val categories = getBase().findFirst()!!.categories
                if (categories.all { it.hidden || it.id == categoryId }) {
                    // Disallow hiding the last non-hidden category
                    return@commitTransaction
                }
            }

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
        commitTransactionForCategory(categoryId) { category ->
            category.icon = icon
        }
    }

    suspend fun addSection(categoryId: CategoryId, name: String): Section {
        val section = Section(name = name.trim())
        commitTransactionForCategory(categoryId) { category ->
            category.sections.add(copy(section))
        }
        return section
    }

    suspend fun moveSection(categoryId: CategoryId, sectionId1: SectionId, sectionId2: SectionId) {
        commitTransactionForCategory(categoryId) { category ->
            category.sections.swap(sectionId1, sectionId2) { id }
        }
    }

    suspend fun updateSection(categoryId: CategoryId, sectionId: SectionId, name: String) {
        commitTransactionForCategory(categoryId) { category ->
            category.sections
                .find { it.id == sectionId }
                ?.name = name.trim()
        }
    }

    suspend fun removeSection(categoryId: CategoryId, sectionId: SectionId) {
        commitTransactionForCategory(categoryId) { category ->
            category.sections
                .find { it.id == sectionId }
                ?.delete()
        }
    }

    private suspend fun commitTransactionForCategory(categoryId: CategoryId, transaction: RealmTransactionContext.(Category) -> Unit) {
        commitTransaction {
            transaction(
                getCategoryById(categoryId)
                    .findFirst()
                    ?: return@commitTransaction,
            )
        }
    }
}
