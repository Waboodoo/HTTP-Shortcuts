package ch.rmy.android.http_shortcuts.activities.categories.editor

import android.app.Application
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.activities.categories.editor.models.CategoryBackground
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryRepository
import ch.rmy.android.http_shortcuts.data.enums.CategoryBackgroundType
import ch.rmy.android.http_shortcuts.data.enums.CategoryLayoutType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutClickBehavior
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination.CategoryEditor.RESULT_CATEGORY_CREATED
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination.CategoryEditor.RESULT_CATEGORY_EDITED
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CategoryEditorViewModel
@Inject
constructor(
    application: Application,
    private val categoryRepository: CategoryRepository,
    private val launcherShortcutManager: LauncherShortcutManager,
) : BaseViewModel<CategoryEditorViewModel.InitData, CategoryEditorViewState>(application) {

    private lateinit var category: Category

    private val isNewCategory
        get() = initData.categoryId == null

    override suspend fun initialize(data: InitData): CategoryEditorViewState {
        category = if (data.categoryId != null) {
            categoryRepository.getCategoryById(data.categoryId)
        } else {
            Category(
                id = newUUID(),
                name = "",
                icon = null,
                layoutType = CategoryLayoutType.LINEAR_LIST,
                background = CategoryBackgroundType.Default,
                hidden = false,
                scale = 1f,
                shortcutClickBehavior = null,
            )
        }

        return CategoryEditorViewState(
            categoryName = category.name,
            categoryLayoutType = category.layoutType,
            categoryBackgroundType = category.background,
            categoryClickBehavior = category.shortcutClickBehavior,
            scale = category.scale,
        )
    }

    fun onCategoryNameChanged(name: String) = runAction {
        updateViewState {
            copy(categoryName = name)
        }
    }

    fun onLayoutTypeChanged(categoryLayoutType: CategoryLayoutType) = runAction {
        updateViewState {
            copy(categoryLayoutType = categoryLayoutType)
        }
    }

    fun onBackgroundChanged(backgroundType: CategoryBackground) = runAction {
        val newCategoryBackgroundType = when (backgroundType) {
            CategoryBackground.DEFAULT -> CategoryBackgroundType.Default
            CategoryBackground.COLOR -> CategoryBackgroundType.Color(viewState.backgroundColor)
        }
        updateViewState {
            copy(categoryBackgroundType = newCategoryBackgroundType)
        }
    }

    fun onClickBehaviorChanged(clickBehavior: ShortcutClickBehavior?) = runAction {
        updateViewState {
            copy(categoryClickBehavior = clickBehavior)
        }
    }

    fun onColorButtonClicked() = runAction {
        updateDialogState(CategoryEditorDialogState.ColorPicker(viewState.backgroundColor))
    }

    fun onBackgroundColorSelected(color: Int) = runAction {
        updateViewState {
            copy(
                categoryBackgroundType = CategoryBackgroundType.Color(color),
                dialogState = null,
            )
        }
    }

    fun onScaleChanged(scale: Float) = runAction {
        updateViewState {
            copy(scale = scale)
        }
    }

    fun onSaveButtonClicked() = runAction {
        if (!viewState.hasChanges) {
            skipAction()
        }
        saveChanges(viewState)
        closeScreen(result = if (isNewCategory) RESULT_CATEGORY_CREATED else RESULT_CATEGORY_EDITED)
    }

    private suspend fun saveChanges(viewState: CategoryEditorViewState) {
        if (isNewCategory) {
            categoryRepository.createCategory(
                name = viewState.categoryName,
                layoutType = viewState.categoryLayoutType,
                background = viewState.categoryBackgroundType,
                clickBehavior = viewState.categoryClickBehavior,
                scale = viewState.scale,
            )
        } else {
            categoryRepository.updateCategory(
                category.id,
                name = viewState.categoryName,
                layoutType = viewState.categoryLayoutType,
                background = viewState.categoryBackgroundType,
                clickBehavior = viewState.categoryClickBehavior,
                scale = viewState.scale,
            )
            launcherShortcutManager.updatePinnedCategoryShortcut(
                category.id,
                viewState.categoryName,
                category.icon ?: ShortcutIcon.BuiltInIcon(DEFAULT_ICON),
            )
        }
    }

    fun onBackPressed() = runAction {
        updateDialogState(CategoryEditorDialogState.DiscardWarning)
    }

    fun onDiscardConfirmed() = runAction {
        updateDialogState(null)
        closeScreen(result = NavigationDestination.RESULT_CHANGES_DISCARDED)
    }

    fun onDialogDismissalRequested() = runAction {
        updateDialogState(null)
    }

    private suspend fun updateDialogState(dialogState: CategoryEditorDialogState?) {
        updateViewState {
            copy(dialogState = dialogState)
        }
    }

    data class InitData(val categoryId: CategoryId?)

    companion object {
        private const val DEFAULT_ICON = "flat_grey_folder"
    }
}
