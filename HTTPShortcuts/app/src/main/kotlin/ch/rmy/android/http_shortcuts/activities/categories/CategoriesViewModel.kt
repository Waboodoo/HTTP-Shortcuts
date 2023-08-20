package ch.rmy.android.http_shortcuts.activities.categories

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.swapped
import ch.rmy.android.framework.extensions.toLocalizable
import ch.rmy.android.framework.utils.localization.QuantityStringLocalizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
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
import kotlinx.coroutines.launch
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

    override fun initViewState() = CategoriesViewState(
        categories = mapCategories(categories),
    )

    override fun onInitializationStarted(data: Unit) {
        viewModelScope.launch {
            categoryRepository.getObservableCategories().collect { categories ->
                this@CategoriesViewModel.categories = categories
                if (isInitialized) {
                    updateViewState {
                        copy(categories = mapCategories(categories))
                    }
                } else {
                    finalizeInitialization()
                }
            }
        }
    }

    fun onBackPressed() {
        viewModelScope.launch {
            waitForOperationsToFinish()
            finish(hasChanged)
        }
    }

    private fun finish(hasChanges: Boolean) {
        finishWithOkResult(
            CategoriesActivity.OpenCategories.createResult(hasChanges),
        )
    }

    fun onCategoryClicked(categoryId: CategoryId) {
        showContextMenu(categoryId)
    }

    private fun showContextMenu(categoryId: CategoryId) {
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

    fun onCategoryMoved(categoryId1: CategoryId, categoryId2: CategoryId) {
        updateViewState {
            copy(categories = categories.swapped(categoryId1, categoryId2) { id })
        }
        launchWithProgressTracking {
            categoryRepository.moveCategory(categoryId1, categoryId2)
            hasChanged = true
        }
    }

    fun onHelpButtonClicked() {
        openURL(ExternalURLs.CATEGORIES_DOCUMENTATION)
    }

    fun onCreateCategoryButtonClicked() {
        emitEvent(CategoriesEvent.OpenCategoryEditor(categoryId = null))
    }

    fun onCategoryVisibilityChanged(visible: Boolean) {
        val categoryId = activeCategoryId ?: return
        updateDialogState(null)
        launchWithProgressTracking {
            categoryRepository.toggleCategoryHidden(categoryId, !visible)
            hasChanged = true
            showSnackbar(if (visible) R.string.message_category_visible else R.string.message_category_hidden)
        }
    }

    fun onCategoryDeletionConfirmed() {
        val categoryId = activeCategoryId ?: return
        updateDialogState(null)
        deleteCategory(categoryId)
    }

    private fun deleteCategory(categoryId: CategoryId) {
        launchWithProgressTracking {
            categoryRepository.deleteCategory(categoryId)
            hasChanged = true
            showSnackbar(R.string.message_category_deleted)
        }
    }

    fun onDeleteClicked() {
        val categoryId = activeCategoryId ?: return
        val category = getCategory(categoryId) ?: return
        updateDialogState(null)
        if (category.shortcuts.isEmpty()) {
            deleteCategory(categoryId)
        } else {
            updateDialogState(CategoriesDialogState.Deletion)
        }
    }

    fun onPlaceOnHomeScreenClicked() {
        val categoryId = activeCategoryId ?: return
        val category = getCategory(categoryId) ?: return
        updateDialogState(CategoriesDialogState.IconPicker(category.icon as? ShortcutIcon.BuiltInIcon))
    }

    fun onCategoryIconSelected(icon: ShortcutIcon) {
        updateDialogState(null)
        onCategoryIconSelected(activeCategoryId ?: return, icon)
        activeCategoryId = null
    }

    private fun onCategoryIconSelected(categoryId: CategoryId, icon: ShortcutIcon) {
        val category = getCategory(categoryId) ?: return
        launchWithProgressTracking {
            categoryRepository.setCategoryIcon(categoryId, icon)
            viewModelScope.launch(Dispatchers.IO) {
                launcherShortcutManager.updatePinnedCategoryShortcut(category.id, category.name, icon)
                launcherShortcutManager.pinCategory(category.id, category.name, icon)
            }
        }
    }

    fun onEditCategoryOptionSelected() {
        val categoryId = activeCategoryId ?: return
        updateDialogState(null)
        emitEvent(CategoriesEvent.OpenCategoryEditor(categoryId))
    }

    fun onCategoryCreated() {
        hasChanged = true
        showSnackbar(R.string.message_category_created)
    }

    fun onCategoryEdited() {
        hasChanged = true
        showSnackbar(R.string.message_category_edited)
    }

    fun onDialogDismissed() {
        updateDialogState(null)
    }

    private fun updateDialogState(dialogState: CategoriesDialogState?) {
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
