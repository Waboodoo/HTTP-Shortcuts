package ch.rmy.android.http_shortcuts.activities.categories.editor

import android.app.Application
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryRepository
import ch.rmy.android.http_shortcuts.data.enums.CategoryBackgroundType
import ch.rmy.android.http_shortcuts.data.enums.CategoryLayoutType
import ch.rmy.android.http_shortcuts.data.models.CategoryModel
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import io.reactivex.Completable

class CategoryEditorViewModel(application: Application) :
    BaseViewModel<CategoryEditorViewModel.InitData, CategoryEditorViewState>(application),
    WithDialog {

    private val categoryRepository = CategoryRepository()
    private val launcherShortcutManager = LauncherShortcutManager(context)

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
            categoryRepository.getCategory(data.categoryId)
                .subscribe(
                    { category ->
                        this.category = category
                        finalizeInitialization()
                    },
                    ::handleInitializationError,
                )
                .attachTo(destroyer)
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
        originalCategoryName = category.name,
        originalCategoryLayoutType = category.categoryLayoutType,
        originalCategoryBackgroundType = category.categoryBackgroundType,
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

    fun onBackgroundChanged(backgroundType: CategoryBackgroundType) {
        doWithViewState { viewState ->
            if (backgroundType == CategoryBackgroundType.WALLPAPER && viewState.categoryBackgroundType != CategoryBackgroundType.WALLPAPER) {
                emitEvent(CategoryEditorEvent.RequestFilePermissionsIfNeeded)
            }
            updateViewState {
                copy(categoryBackgroundType = backgroundType)
            }
        }
    }

    fun onSaveButtonClicked() {
        doWithViewState { viewState ->
            if (!viewState.hasChanges) {
                return@doWithViewState
            }
            performOperation(saveChanges(viewState)) {
                finishWithOkResult()
            }
        }
    }

    private fun saveChanges(viewState: CategoryEditorViewState): Completable =
        if (isNewCategory) {
            categoryRepository.createCategory(viewState.categoryName)
        } else {
            categoryRepository.updateCategory(
                category.id,
                name = viewState.categoryName,
                layoutType = viewState.categoryLayoutType,
                background = viewState.categoryBackgroundType,
            )
                .andThen(
                    Completable.fromAction {
                        launcherShortcutManager.updatePinnedCategoryShortcut(category.id, viewState.categoryName)
                    }
                )
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
