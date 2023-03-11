package ch.rmy.android.http_shortcuts.activities.categories

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.swapped
import ch.rmy.android.framework.extensions.toLocalizable
import ch.rmy.android.framework.utils.localization.QuantityStringLocalizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.categories.usecases.GetContextMenuDialogUseCase
import ch.rmy.android.http_shortcuts.activities.categories.usecases.GetDeletionDialogUseCase
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryRepository
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.utils.ExternalURLs
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import kotlinx.coroutines.launch
import javax.inject.Inject

class CategoriesViewModel(application: Application) : BaseViewModel<Unit, CategoriesViewState>(application), WithDialog {

    @Inject
    lateinit var categoryRepository: CategoryRepository

    @Inject
    lateinit var launcherShortcutManager: LauncherShortcutManager

    @Inject
    lateinit var getContextMenuDialog: GetContextMenuDialogUseCase

    @Inject
    lateinit var getDeletionDialog: GetDeletionDialogUseCase

    init {
        getApplicationComponent().inject(this)
    }

    private lateinit var categories: List<Category>
    private var hasChanged = false

    override var dialogState: DialogState?
        get() = currentViewState?.dialogState
        set(value) {
            updateViewState {
                copy(dialogState = value)
            }
        }

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
        dialogState = getContextMenuDialog(
            categoryId = category.id,
            title = category.name.toLocalizable(),
            hideOptionVisible = !category.hidden && categories.count { !it.hidden } > 1,
            showOptionVisible = category.hidden,
            placeOnHomeScreenOptionVisible = !category.hidden && launcherShortcutManager.supportsPinning(),
            deleteOptionVisible = category.hidden || categories.count { !it.hidden } > 1,
            viewModel = this,
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

    fun onCategoryVisibilityChanged(categoryId: CategoryId, hidden: Boolean) {
        launchWithProgressTracking {
            categoryRepository.toggleCategoryHidden(categoryId, hidden)
            hasChanged = true
            showSnackbar(if (hidden) R.string.message_category_hidden else R.string.message_category_visible)
        }
    }

    fun onCategoryDeletionConfirmed(categoryId: CategoryId) {
        launchWithProgressTracking {
            categoryRepository.deleteCategory(categoryId)
            hasChanged = true
            showSnackbar(R.string.message_category_deleted)
        }
    }

    fun onCategoryDeletionSelected(categoryId: CategoryId) {
        val category = getCategory(categoryId) ?: return
        if (category.shortcuts.isEmpty()) {
            onCategoryDeletionConfirmed(categoryId)
        } else {
            dialogState = getDeletionDialog(categoryId, this)
        }
    }

    fun onPlaceOnHomeScreenSelected(categoryId: CategoryId) {
        val category = getCategory(categoryId) ?: return
        launcherShortcutManager.pinCategory(category.id, category.name)
    }

    fun onEditCategoryOptionSelected(categoryId: CategoryId) {
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
