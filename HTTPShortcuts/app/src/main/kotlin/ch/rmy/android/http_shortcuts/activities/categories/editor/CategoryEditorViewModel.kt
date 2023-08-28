package ch.rmy.android.http_shortcuts.activities.categories.editor

import android.app.Application
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.activities.categories.editor.models.CategoryBackground
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryRepository
import ch.rmy.android.http_shortcuts.data.enums.CategoryBackgroundType
import ch.rmy.android.http_shortcuts.data.enums.CategoryLayoutType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutClickBehavior
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import javax.inject.Inject

class CategoryEditorViewModel(application: Application) :
    BaseViewModel<CategoryEditorViewModel.InitData, CategoryEditorViewState>(application) {

    @Inject
    lateinit var categoryRepository: CategoryRepository

    @Inject
    lateinit var launcherShortcutManager: LauncherShortcutManager

    init {
        getApplicationComponent().inject(this)
    }

    private lateinit var category: Category

    private val isNewCategory
        get() = initData.categoryId == null

    override suspend fun initialize(data: InitData): CategoryEditorViewState {
        category = if (data.categoryId != null) {
            categoryRepository.getCategory(data.categoryId)
        } else {
            Category()
        }

        return CategoryEditorViewState(
            categoryName = category.name,
            categoryLayoutType = category.categoryLayoutType,
            categoryBackgroundType = category.categoryBackgroundType,
            categoryClickBehavior = category.clickBehavior,
            originalCategoryName = category.name,
            originalCategoryLayoutType = category.categoryLayoutType,
            originalCategoryBackgroundType = category.categoryBackgroundType,
            originalCategoryClickBehavior = category.clickBehavior,
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

    fun onSaveButtonClicked() = runAction {
        if (!viewState.hasChanges) {
            skipAction()
        }
        saveChanges(viewState)
        finishWithOkResult()
    }

    private suspend fun saveChanges(viewState: CategoryEditorViewState) {
        if (isNewCategory) {
            categoryRepository.createCategory(
                name = viewState.categoryName,
                layoutType = viewState.categoryLayoutType,
                background = viewState.categoryBackgroundType,
                clickBehavior = viewState.categoryClickBehavior,
            )
        } else {
            categoryRepository.updateCategory(
                category.id,
                name = viewState.categoryName,
                layoutType = viewState.categoryLayoutType,
                background = viewState.categoryBackgroundType,
                clickBehavior = viewState.categoryClickBehavior,
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
        finish()
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
