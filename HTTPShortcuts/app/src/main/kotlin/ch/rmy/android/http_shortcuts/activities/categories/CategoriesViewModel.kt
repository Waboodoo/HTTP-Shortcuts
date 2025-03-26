package ch.rmy.android.http_shortcuts.activities.categories

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.swapped
import ch.rmy.android.framework.extensions.toLocalizable
import ch.rmy.android.framework.utils.localization.QuantityStringLocalizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.ViewModelScope
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.categories.models.CategoryListItem
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryRepository
import ch.rmy.android.http_shortcuts.data.domains.sections.SectionRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.Section
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination.Categories.RESULT_CATEGORIES_CHANGED
import ch.rmy.android.http_shortcuts.utils.ExternalURLs
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class CategoriesViewModel
@Inject
constructor(
    application: Application,
    private val categoryRepository: CategoryRepository,
    private val sectionRepository: SectionRepository,
    private val shortcutRepository: ShortcutRepository,
    private val launcherShortcutManager: LauncherShortcutManager,
) : BaseViewModel<Unit, CategoriesViewState>(application) {

    private lateinit var categories: List<Category>
    private var hasChanged = false
    private var activeCategoryId: CategoryId? = null
    private lateinit var sectionsByCategoryId: Map<CategoryId, List<Section>>
    private lateinit var shortcutsByCategoryId: Map<CategoryId, List<Shortcut>>

    override suspend fun initialize(data: Unit): CategoriesViewState {
        sectionsByCategoryId = sectionRepository.getSections().groupBy { it.categoryId }
        shortcutsByCategoryId = shortcutRepository.getShortcuts().groupBy { it.categoryId }
        val categoriesFlow = categoryRepository.observeCategories()
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
        closeScreen(result = if (hasChanged) RESULT_CATEGORIES_CHANGED else null)
    }

    fun onCategoryClicked(categoryId: CategoryId) = runAction {
        showContextMenu(categoryId)
    }

    private suspend fun showContextMenu(categoryId: CategoryId) {
        val category = getCategory(categoryId) ?: return
        activeCategoryId = categoryId
        val hasEnoughUnhiddenCategories = categories.count { !it.hidden } > 1
        updateDialogState(
            CategoriesDialogState.ContextMenu(
                title = category.name.toLocalizable(),
                hideOptionVisible = !category.hidden,
                showOptionVisible = category.hidden,
                placeOnHomeScreenOptionVisible = !category.hidden && launcherShortcutManager.supportsPinning(),
                hideOptionEnabled = hasEnoughUnhiddenCategories,
                deleteOptionEnabled = category.hidden || hasEnoughUnhiddenCategories,
            ),
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
        navigate(NavigationDestination.CategoryEditor.buildRequest(categoryId = null))
    }

    fun onCategoryVisibilityChanged(visible: Boolean) = runAction {
        val categoryId = activeCategoryId ?: skipAction()
        updateDialogState(null)
        withProgressTracking {
            categoryRepository.setCategoryHidden(categoryId, !visible)
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
        if (shortcutsByCategoryId[categoryId].isNullOrEmpty()) {
            deleteCategory(categoryId)
        } else {
            updateDialogState(CategoriesDialogState.Deletion(category.name))
        }
    }

    fun onPlaceOnHomeScreenClicked() = runAction {
        val categoryId = activeCategoryId ?: skipAction()
        val category = getCategory(categoryId) ?: skipAction()
        updateDialogState(
            CategoriesDialogState.IconPicker(
                currentIcon = (category.icon as? ShortcutIcon.BuiltInIcon)
                    ?: ShortcutIcon.BuiltInIcon.fromDrawableResource(context, R.drawable.flat_grey_folder),
                suggestionBase = category.name,
            ),
        )
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
        navigate(NavigationDestination.CategoryEditor.buildRequest(categoryId))
    }

    fun onManageSectionsClicked() = runAction {
        val categoryId = activeCategoryId ?: skipAction()
        updateDialogState(null)
        hasChanged = true // I'm too lazy to implement this, let's just assume that there might be changes
        navigate(NavigationDestination.CategorySectionsEditor.buildRequest(categoryId))
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

    fun onCustomIconOptionSelected() = runAction {
        updateDialogState(null)
        navigate(NavigationDestination.IconPicker)
    }

    fun onChangesDiscarded() = runAction {
        showSnackbar(R.string.message_changes_discarded)
    }

    private fun mapCategories(categories: List<Category>): List<CategoryListItem> =
        categories.map { category ->
            val sections = sectionsByCategoryId[category.id] ?: emptyList()
            val shortcuts = shortcutsByCategoryId[category.id] ?: emptyList()
            CategoryListItem(
                id = category.id,
                name = if (category.hidden) {
                    StringResLocalizable(R.string.label_category_hidden, category.name)
                } else {
                    category.name.toLocalizable()
                },
                description = if (sections.isNotEmpty()) {
                    StringResLocalizable(
                        R.string.shortcut_section_count_pattern,
                        QuantityStringLocalizable(
                            R.plurals.shortcut_count,
                            count = shortcuts.size,
                        ),
                        QuantityStringLocalizable(
                            R.plurals.section_count,
                            count = sections.size,
                        ),
                    )
                } else {
                    QuantityStringLocalizable(
                        R.plurals.shortcut_count,
                        count = shortcuts.size,
                    )
                },
                icons = shortcuts
                    .take(MAX_ICONS)
                    .map { shortcut ->
                        shortcut.icon
                    },
                layoutType = category.layoutType.takeUnless { category.hidden },
                hidden = category.hidden,
            )
        }

    companion object {
        private const val MAX_ICONS = 5
    }
}
