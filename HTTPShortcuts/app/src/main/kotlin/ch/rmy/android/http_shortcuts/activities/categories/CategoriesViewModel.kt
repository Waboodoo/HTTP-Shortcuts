package ch.rmy.android.http_shortcuts.activities.categories

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.swapped
import ch.rmy.android.framework.extensions.toLocalizable
import ch.rmy.android.framework.utils.localization.QuantityStringLocalizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.ViewModelScope
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.categories.models.CategoryListItem
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryRepository
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.utils.ExternalURLs
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CategoriesViewModel(application: Application) : BaseViewModel<Unit, CategoriesViewState>(application) {

    @Inject
    lateinit var categoryRepository: CategoryRepository

    @Inject
    lateinit var launcherShortcutManager: LauncherShortcutManager

    init {
        getApplicationComponent().inject(this)
    }

    private lateinit var categories: List<Category>
    private var hasChanged = false
    private var activeCategoryId: CategoryId? = null

    override suspend fun initialize(data: Unit): CategoriesViewState {
        val categoriesFlow = categoryRepository.getObservableCategories()
        categories = categoriesFlow.first()
        viewModelScope.launch {
            categoriesFlow.collect { categories ->
                this@CategoriesViewModel.categories = categories
                updateViewState {
                    copy(categories = mapCategories(categories))
                }
            }
        }
        return CategoriesViewState(
            categories = mapCategories(categories),
        )
    }

    fun onBackPressed() = runAction {
        waitForOperationsToFinish()
        finish(hasChanged)
    }

    private suspend fun finish(hasChanges: Boolean) {
        finishWithOkResult(
            CategoriesActivity.OpenCategories.createResult(hasChanges),
        )
    }

    fun onCategoryClicked(categoryId: CategoryId) = runAction {
        showContextMenu(categoryId)
    }

    private suspend fun showContextMenu(categoryId: CategoryId) {
        val category = getCategory(categoryId) ?: return
        activeCategoryId = categoryId
        updateDialogState(
            CategoriesDialogState.ContextMenu(
                title = category.name.toLocalizable(),
                hideOptionVisible = !category.hidden && categories.count { !it.hidden } > 1,
                showOptionVisible = category.hidden,
                placeOnHomeScreenOptionVisible = !category.hidden && launcherShortcutManager.supportsPinning(),
                deleteOptionEnabled = category.hidden || categories.count { !it.hidden } > 1,
            )
        )
    }

    private fun getCategory(categoryId: CategoryId) =
        categories.firstOrNull { it.id == categoryId }

    fun onCategoryMoved(categoryId1: CategoryId, categoryId2: CategoryId) = runAction {
        updateViewState {
            copy(categories = categories.swapped(categoryId1, categoryId2) { id })
        }
        withProgressTracking {
            categoryRepository.moveCategory(categoryId1, categoryId2)
            hasChanged = true
        }
    }

    fun onHelpButtonClicked() = runAction {
        openURL(ExternalURLs.CATEGORIES_DOCUMENTATION)
    }

    fun onCreateCategoryButtonClicked() = runAction {
        emitEvent(CategoriesEvent.OpenCategoryEditor(categoryId = null))
    }

    fun onCategoryVisibilityChanged(visible: Boolean) = runAction {
        val categoryId = activeCategoryId ?: skipAction()
        updateDialogState(null)
        withProgressTracking {
            categoryRepository.toggleCategoryHidden(categoryId, !visible)
            hasChanged = true
            showSnackbar(if (visible) R.string.message_category_visible else R.string.message_category_hidden)
        }
    }

    fun onCategoryDeletionConfirmed() = runAction {
        val categoryId = activeCategoryId ?: skipAction()
        updateDialogState(null)
        deleteCategory(categoryId)
    }

    private suspend fun ViewModelScope<*>.deleteCategory(categoryId: CategoryId) {
        withProgressTracking {
            categoryRepository.deleteCategory(categoryId)
            hasChanged = true
            showSnackbar(R.string.message_category_deleted)
        }
    }

    fun onDeleteClicked() = runAction {
        val categoryId = activeCategoryId ?: skipAction()
        val category = getCategory(categoryId) ?: skipAction()
        updateDialogState(null)
        if (category.shortcuts.isEmpty()) {
            deleteCategory(categoryId)
        } else {
            updateDialogState(CategoriesDialogState.Deletion)
        }
    }

    fun onPlaceOnHomeScreenClicked() = runAction {
        val categoryId = activeCategoryId ?: skipAction()
        val category = getCategory(categoryId) ?: skipAction()
        updateDialogState(CategoriesDialogState.IconPicker(category.icon as? ShortcutIcon.BuiltInIcon))
    }

    fun onCategoryIconSelected(icon: ShortcutIcon) = runAction {
        updateDialogState(null)
        onCategoryIconSelected(activeCategoryId ?: skipAction(), icon)
        activeCategoryId = null
    }

    private suspend fun ViewModelScope<*>.onCategoryIconSelected(categoryId: CategoryId, icon: ShortcutIcon) {
        val category = getCategory(categoryId) ?: return
        withProgressTracking {
            categoryRepository.setCategoryIcon(categoryId, icon)
            withContext(Dispatchers.Default) {
                launcherShortcutManager.updatePinnedCategoryShortcut(category.id, category.name, icon)
                launcherShortcutManager.pinCategory(category.id, category.name, icon)
            }
        }
    }

    fun onEditCategoryOptionSelected() = runAction {
        val categoryId = activeCategoryId ?: skipAction()
        updateDialogState(null)
        emitEvent(CategoriesEvent.OpenCategoryEditor(categoryId))
    }

    fun onCategoryCreated() = runAction {
        hasChanged = true
        showSnackbar(R.string.message_category_created)
    }

    fun onCategoryEdited() = runAction {
        hasChanged = true
        showSnackbar(R.string.message_category_edited)
    }

    fun onDialogDismissed() = runAction {
        updateDialogState(null)
    }

    private suspend fun updateDialogState(dialogState: CategoriesDialogState?) {
        updateViewState {
            copy(dialogState = dialogState)
        }
    }

    companion object {
        private const val MAX_ICONS = 5

        internal fun mapCategories(categories: List<Category>): List<CategoryListItem> =
            categories.map { category ->
                CategoryListItem(
                    id = category.id,
                    name = if (category.hidden) {
                        StringResLocalizable(R.string.label_category_hidden, category.name)
                    } else {
                        category.name.toLocalizable()
                    },
                    description = QuantityStringLocalizable(
                        R.plurals.shortcut_count,
                        count = category.shortcuts.size,
                    ),
                    icons = category.shortcuts
                        .take(MAX_ICONS)
                        .map { shortcut ->
                            shortcut.icon
                        },
                    layoutType = category.categoryLayoutType.takeUnless { category.hidden },
                    hidden = category.hidden,
                )
            }
    }
}
