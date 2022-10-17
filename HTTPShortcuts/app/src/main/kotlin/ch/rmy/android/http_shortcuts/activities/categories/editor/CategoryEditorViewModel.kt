package ch.rmy.android.http_shortcuts.activities.categories.editor

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.categories.editor.models.CategoryBackground
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryRepository
import ch.rmy.android.http_shortcuts.data.enums.CategoryBackgroundType
import ch.rmy.android.http_shortcuts.data.enums.CategoryLayoutType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutClickBehavior
import ch.rmy.android.http_shortcuts.data.models.CategoryModel
import ch.rmy.android.http_shortcuts.utils.ColorPickerFactory
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import javax.inject.Inject

class CategoryEditorViewModel(application: Application) :
    BaseViewModel<CategoryEditorViewModel.InitData, CategoryEditorViewState>(application),
    WithDialog {

    @Inject
    lateinit var categoryRepository: CategoryRepository

    @Inject
    lateinit var launcherShortcutManager: LauncherShortcutManager

    @Inject
    lateinit var colorPickerFactory: ColorPickerFactory

    init {
        getApplicationComponent().inject(this)
    }

    private lateinit var category: CategoryModel

    private val isNewCategory
        get() = initData.categoryId == null

    override var dialogState: DialogState?
        get() = currentViewState?.dialogState
        set(value) {
            updateViewState {
                copy(dialogState = value)
            }
        }

    override fun onInitializationStarted(data: InitData) {
        if (data.categoryId != null) {
            viewModelScope.launch {
                try {
                    val category = categoryRepository.getCategory(data.categoryId)
                    this@CategoryEditorViewModel.category = category
                    finalizeInitialization()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    handleInitializationError(e)
                }
            }
        } else {
            category = CategoryModel()
            finalizeInitialization()
        }
    }

    private fun handleInitializationError(error: Throwable) {
        handleUnexpectedError(error)
        finish()
    }

    override fun initViewState() = CategoryEditorViewState(
        toolbarTitle = StringResLocalizable(if (isNewCategory) R.string.title_create_category else R.string.title_edit_category),
        categoryName = category.name,
        categoryLayoutType = category.categoryLayoutType,
        categoryBackgroundType = category.categoryBackgroundType,
        categoryClickBehavior = category.clickBehavior,
        originalCategoryName = category.name,
        originalCategoryLayoutType = category.categoryLayoutType,
        originalCategoryBackgroundType = category.categoryBackgroundType,
        originalCategoryClickBehavior = category.clickBehavior,
    )

    fun onCategoryNameChanged(name: String) {
        updateViewState {
            copy(categoryName = name)
        }
    }

    fun onLayoutTypeChanged(categoryLayoutType: CategoryLayoutType) {
        updateViewState {
            copy(categoryLayoutType = categoryLayoutType)
        }
    }

    fun onBackgroundChanged(backgroundType: CategoryBackground) {
        doWithViewState { viewState ->
            if (backgroundType == CategoryBackground.WALLPAPER && viewState.categoryBackground != CategoryBackground.WALLPAPER) {
                emitEvent(CategoryEditorEvent.RequestFilePermissionsIfNeeded)
            }
            val newCategoryBackgroundType = when (backgroundType) {
                CategoryBackground.DEFAULT -> CategoryBackgroundType.Default
                CategoryBackground.COLOR -> CategoryBackgroundType.Color(viewState.backgroundColor)
                CategoryBackground.WALLPAPER -> CategoryBackgroundType.Wallpaper
            }
            updateViewState {
                copy(categoryBackgroundType = newCategoryBackgroundType)
            }
        }
    }

    fun onClickBehaviorChanged(clickBehavior: ShortcutClickBehavior?) {
        updateViewState {
            copy(categoryClickBehavior = clickBehavior)
        }
    }

    fun onColorButtonClicked() {
        doWithViewState { viewState ->
            dialogState = DialogState.create("category-color-picker") {
                colorPickerFactory.createColorPicker(
                    onColorPicked = ::onBackgroundColorSelected,
                    onDismissed = {
                        dialogState?.let(::onDialogDismissed)
                    },
                    initialColor = viewState.backgroundColor,
                )
            }
        }
    }

    private fun onBackgroundColorSelected(color: Int) {
        updateViewState {
            copy(categoryBackgroundType = CategoryBackgroundType.Color(color))
        }
    }

    fun onSaveButtonClicked() {
        doWithViewState { viewState ->
            if (!viewState.hasChanges) {
                return@doWithViewState
            }
            viewModelScope.launch {
                saveChanges(viewState)
                finishWithOkResult()
            }
        }
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
            launcherShortcutManager.updatePinnedCategoryShortcut(category.id, viewState.categoryName)
        }
    }

    fun onBackPressed() {
        doWithViewState { viewState ->
            if (viewState.hasChanges) {
                showDiscardDialog()
            } else {
                finish()
            }
        }
    }

    private fun showDiscardDialog() {
        dialogState = DialogState.create {
            message(R.string.confirm_discard_changes_message)
                .positive(R.string.dialog_discard) { onDiscardDialogConfirmed() }
                .negative(R.string.dialog_cancel)
                .build()
        }
    }

    private fun onDiscardDialogConfirmed() {
        finish()
    }

    data class InitData(val categoryId: CategoryId?)
}
