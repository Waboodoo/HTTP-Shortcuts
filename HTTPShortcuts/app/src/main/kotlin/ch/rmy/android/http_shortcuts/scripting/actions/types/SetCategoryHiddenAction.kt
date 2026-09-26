package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryRepository
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.sync.SyncScheduler
import javax.inject.Inject

class SetCategoryHiddenAction
@Inject
constructor(
    private val categoryRepository: CategoryRepository,
    private val syncScheduler: SyncScheduler,
) : Action<SetCategoryHiddenAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext) {
        setHidden(this.categoryNameOrId ?: "")
    }

    private suspend fun Params.setHidden(categoryNameOrId: String) {
        val category = try {
            categoryRepository.getCategoryByNameOrId(categoryNameOrId)
        } catch (_: NoSuchElementException) {
            throw ActionException {
                getString(R.string.error_category_not_found_for_set_visible, categoryNameOrId)
            }
        }
        if (hidden == category.hidden) {
            return
        }
        categoryRepository.setCategoryHidden(category.id, hidden)
        syncScheduler.syncSoonOnChangesIfNeeded()
    }

    data class Params(
        val categoryNameOrId: String?,
        val hidden: Boolean,
    )
}
