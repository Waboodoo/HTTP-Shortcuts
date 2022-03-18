package ch.rmy.android.http_shortcuts.activities.main

import android.app.Application
import android.net.Uri
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.swapped
import ch.rmy.android.framework.extensions.toLocalizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.EventBridge
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.activities.editor.ShortcutEditorActivity
import ch.rmy.android.http_shortcuts.activities.main.usecases.GetContextMenuDialogUseCase
import ch.rmy.android.http_shortcuts.activities.main.usecases.GetCurlExportDialogUseCase
import ch.rmy.android.http_shortcuts.activities.main.usecases.GetExportOptionsDialogUseCase
import ch.rmy.android.http_shortcuts.activities.main.usecases.GetMoveOptionsDialogUseCase
import ch.rmy.android.http_shortcuts.activities.main.usecases.GetMoveToCategoryDialogUseCase
import ch.rmy.android.http_shortcuts.activities.main.usecases.GetShortcutDeletionDialogUseCase
import ch.rmy.android.http_shortcuts.activities.main.usecases.GetShortcutInfoDialogUseCase
import ch.rmy.android.http_shortcuts.data.domains.app.AppRepository
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryRepository
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.PendingExecutionsRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.domains.widgets.WidgetsRepository
import ch.rmy.android.http_shortcuts.data.enums.CategoryBackgroundType
import ch.rmy.android.http_shortcuts.data.enums.SelectionMode
import ch.rmy.android.http_shortcuts.data.models.CategoryModel
import ch.rmy.android.http_shortcuts.data.models.PendingExecutionModel
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.data.models.VariableModel
import ch.rmy.android.http_shortcuts.exceptions.CanceledByUserException
import ch.rmy.android.http_shortcuts.extensions.toLauncherShortcut
import ch.rmy.android.http_shortcuts.extensions.type
import ch.rmy.android.http_shortcuts.import_export.CurlExporter
import ch.rmy.android.http_shortcuts.import_export.ExportFormat
import ch.rmy.android.http_shortcuts.scheduling.ExecutionScheduler
import ch.rmy.android.http_shortcuts.utils.Settings
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.VariableResolver
import ch.rmy.curlcommand.CurlCommand

class ShortcutListViewModel(
    application: Application,
) : BaseViewModel<ShortcutListViewModel.InitData, ShortcutListViewState>(application), WithDialog {

    private val appRepository = AppRepository()
    private val shortcutRepository = ShortcutRepository()
    private val categoryRepository = CategoryRepository()
    private val variableRepository = VariableRepository()
    private val pendingExecutionsRepository = PendingExecutionsRepository()
    private val widgetsRepository = WidgetsRepository()
    private val curlExporter = CurlExporter(context)
    private val eventBridge = EventBridge(ChildViewModelEvent::class.java)
    private val executionScheduler = ExecutionScheduler(application)
    private val settings = Settings(context)
    private val getCurlExportDialog = GetCurlExportDialogUseCase()
    private val getShortcutInfoDialog = GetShortcutInfoDialogUseCase()
    private val getShortcutDeletionDialog = GetShortcutDeletionDialogUseCase()
    private val getExportOptionsDialog = GetExportOptionsDialogUseCase()
    private val getMoveOptionsDialog = GetMoveOptionsDialogUseCase()
    private val getContextMenuDialog = GetContextMenuDialogUseCase()
    private val getMoveToCategoryDialog = GetMoveToCategoryDialogUseCase()

    private lateinit var category: CategoryModel
    private var categories: List<CategoryModel> = emptyList()
    private var variables: List<VariableModel> = emptyList()
    private var pendingShortcuts: List<PendingExecutionModel> = emptyList()

    private var exportingShortcutId: String? = null
    private var isAppLocked = false

    override var dialogState: DialogState?
        get() = currentViewState.dialogState
        set(value) {
            updateViewState {
                copy(dialogState = value)
            }
        }

    override fun onInitializationStarted(data: InitData) {
        categoryRepository.getObservableCategory(data.categoryId)
            .subscribe { category ->
                this.category = category
                if (isInitialized) {
                    recomputeShortcutList()
                } else {
                    finalizeInitialization()
                }
            }
            .attachTo(destroyer)

        categoryRepository.getCategories()
            .subscribe { categories ->
                this.categories = categories
            }
            .attachTo(destroyer)

        variableRepository.getObservableVariables()
            .subscribe { variables ->
                this.variables = variables
            }
            .attachTo(destroyer)

        pendingExecutionsRepository.getObservablePendingExecutions()
            .subscribe { pendingShortcuts ->
                this.pendingShortcuts = pendingShortcuts
                if (isInitialized) {
                    recomputeShortcutList()
                }
            }
            .attachTo(destroyer)

        appRepository.getObservableLock()
            .subscribe { lockOptional ->
                isAppLocked = lockOptional.value != null
                if (isInitialized) {
                    updateViewState {
                        copy(isAppLocked = this@ShortcutListViewModel.isAppLocked)
                    }
                }
            }
            .attachTo(destroyer)
    }

    override fun initViewState() = ShortcutListViewState(
        isAppLocked = isAppLocked,
        shortcuts = mapShortcuts(),
        background = category.categoryBackgroundType,
    )

    private fun recomputeShortcutList() {
        updateViewState {
            copy(shortcuts = mapShortcuts())
        }
    }

    private fun mapShortcuts(): List<ShortcutListItem> {
        val shortcuts = category.shortcuts
        return if (shortcuts.isEmpty()) {
            listOf(ShortcutListItem.EmptyState)
        } else {
            val textColor = when (category.categoryBackgroundType) {
                CategoryBackgroundType.WHITE -> ShortcutListItem.TextColor.DARK
                CategoryBackgroundType.BLACK -> ShortcutListItem.TextColor.BRIGHT
                CategoryBackgroundType.WALLPAPER -> ShortcutListItem.TextColor.BRIGHT
            }
            shortcuts.map { shortcut ->
                ShortcutListItem.Shortcut(
                    id = shortcut.id,
                    name = shortcut.name,
                    description = shortcut.description,
                    icon = shortcut.icon,
                    isPending = pendingShortcuts.any { it.shortcutId == shortcut.id },
                    textColor = textColor,
                )
            }
        }
    }

    fun onPaused() {
        if (isInitialized && currentViewState.isInMovingMode) {
            disableMovingMode()
        }
    }

    fun onBackPressed(): Boolean =
        if (isInitialized && currentViewState.isInMovingMode) {
            disableMovingMode()
            true
        } else {
            false
        }

    fun onMoveModeOptionSelected() {
        enableMovingMode()
    }

    private fun enableMovingMode() {
        updateViewState {
            copy(isInMovingMode = true)
        }
        eventBridge.submit(ChildViewModelEvent.MovingModeChanged(true))
        showSnackbar(R.string.message_moving_enabled, long = true)
    }

    private fun disableMovingMode() {
        updateViewState {
            copy(isInMovingMode = false)
        }
        eventBridge.submit(ChildViewModelEvent.MovingModeChanged(false))
    }

    fun onShortcutMoved(shortcutId1: String, shortcutId2: String) {
        updateViewState {
            copy(shortcuts = shortcuts.swapped(shortcutId1, shortcutId2) { (this as? ShortcutListItem.Shortcut)?.id })
        }
        performOperation(
            shortcutRepository.swapShortcutPositions(category.id, shortcutId1, shortcutId2)
        )
    }

    fun onShortcutClicked(shortcutId: String) {
        logInfo("Shortcut clicked")
        if (currentViewState.isInMovingMode) {
            showSnackbar(R.string.message_moving_enabled)
            return
        }
        if (initData.selectionMode != SelectionMode.NORMAL) {
            selectShortcut(shortcutId)
            return
        }
        if (currentViewState.isAppLocked) {
            executeShortcut(shortcutId)
            return
        }
        when (settings.clickBehavior) {
            Settings.CLICK_BEHAVIOR_RUN -> executeShortcut(shortcutId)
            Settings.CLICK_BEHAVIOR_EDIT -> editShortcut(shortcutId)
            Settings.CLICK_BEHAVIOR_MENU -> showContextMenu(shortcutId)
        }
    }

    private fun selectShortcut(shortcutId: String) {
        eventBridge.submit(ChildViewModelEvent.SelectShortcut(shortcutId))
    }

    private fun executeShortcut(shortcutId: String) {
        logInfo("Preparing to execute shortcut")
        openActivity(ExecuteActivity.IntentBuilder(shortcutId))
    }

    private fun editShortcut(shortcutId: String) {
        logInfo("Preparing to edit shortcut")
        openActivity(
            ShortcutEditorActivity.IntentBuilder()
                .categoryId(category.id)
                .shortcutId(shortcutId),
            requestCode = ShortcutListFragment.REQUEST_EDIT_SHORTCUT,
        )
    }

    private fun showContextMenu(shortcutId: String) {
        val shortcut = getShortcutById(shortcutId) ?: return
        dialogState = getContextMenuDialog(
            shortcutId,
            title = shortcut.name,
            isPending = pendingShortcuts.any { it.shortcutId == shortcut.id },
            isMovable = canMoveWithinCategory() || canMoveAcrossCategories(),
            viewModel = this,
        )
    }

    private fun canMoveWithinCategory() =
        category.shortcuts.size > 1

    private fun canMoveAcrossCategories() =
        categories.size > 1

    fun onShortcutLongClicked(shortcutId: String) {
        if (currentViewState.isLongClickingEnabled) {
            showContextMenu(shortcutId)
        }
    }

    private fun getShortcutById(shortcutId: String): ShortcutModel? =
        category.shortcuts.firstOrNull { it.id == shortcutId }

    fun onPlaceOnHomeScreenOptionSelected(shortcutId: String) {
        val shortcut = getShortcutById(shortcutId) ?: return
        eventBridge.submit(ChildViewModelEvent.PlaceShortcutOnHomeScreen(shortcut.toLauncherShortcut()))
    }

    fun onExecuteOptionSelected(shortcutId: String) {
        executeShortcut(shortcutId)
    }

    fun onCancelPendingExecutionOptionSelected(shortcutId: String) {
        cancelPendingExecution(shortcutId)
    }

    private fun cancelPendingExecution(shortcutId: String) {
        val shortcut = getShortcutById(shortcutId) ?: return
        pendingExecutionsRepository.removePendingExecutionsForShortcut(shortcutId)
            .andThen(executionScheduler.schedule())
            .subscribe {
                showSnackbar(StringResLocalizable(R.string.pending_shortcut_execution_cancelled, shortcut.name))
            }
            .attachTo(destroyer)
    }

    fun onEditOptionSelected(shortcutId: String) {
        editShortcut(shortcutId)
    }

    fun onMoveOptionSelected(shortcutId: String) {
        val canMoveWithinCategory = canMoveWithinCategory()
        val canMoveAcrossCategories = canMoveAcrossCategories()
        when {
            canMoveWithinCategory && canMoveAcrossCategories -> showMoveOptionsDialog(shortcutId)
            canMoveWithinCategory -> enableMovingMode()
            canMoveAcrossCategories -> onMoveToCategoryOptionSelected(shortcutId)
        }
    }

    private fun showMoveOptionsDialog(shortcutId: String) {
        dialogState = getMoveOptionsDialog(shortcutId, this)
    }

    fun onDuplicateOptionSelected(shortcutId: String) {
        duplicateShortcut(shortcutId)
    }

    private fun duplicateShortcut(shortcutId: String) {
        val shortcut = getShortcutById(shortcutId) ?: return
        val name = shortcut.name
        val newName = context.getString(R.string.template_shortcut_name_copy, shortcut.name)
        val categoryId = category.id

        val newPosition = category.shortcuts
            .indexOfFirst { it.id == shortcut.id }
            .takeIf { it != -1 }
            ?.let { it + 1 }

        performOperation(
            shortcutRepository.duplicateShortcut(shortcutId, newName, newPosition, categoryId)
        ) {
            showSnackbar(StringResLocalizable(R.string.shortcut_duplicated, name))
        }
    }

    fun onDeleteOptionSelected(shortcutId: String) {
        showDeletionDialog(getShortcutById(shortcutId) ?: return)
    }

    private fun showDeletionDialog(shortcut: ShortcutModel) {
        dialogState = getShortcutDeletionDialog(shortcut.id, shortcut.name.toLocalizable(), this)
    }

    fun onShowInfoOptionSelected(shortcutId: String) {
        showShortcutInfoDialog(getShortcutById(shortcutId) ?: return)
    }

    private fun showShortcutInfoDialog(shortcut: ShortcutModel) {
        dialogState = getShortcutInfoDialog(shortcut.id, shortcut.name)
    }

    fun onExportOptionSelected(shortcutId: String) {
        val shortcut = getShortcutById(shortcutId) ?: return

        if (shortcut.type.usesUrl) {
            showExportOptionsDialog(shortcutId)
        } else {
            showFileExportDialog(shortcutId)
        }
    }

    private fun showExportOptionsDialog(shortcutId: String) {
        dialogState = getExportOptionsDialog(shortcutId, this)
    }

    fun onExportAsCurlOptionSelected(shortcutId: String) {
        val shortcut = getShortcutById(shortcutId) ?: return
        curlExporter.generateCommand(shortcut)
            .subscribe(
                { command ->
                    showCurlExportDialog(shortcut.name, command)
                },
                { e ->
                    if (e !is CanceledByUserException) {
                        showToast(R.string.error_generic)
                        logException(e)
                    }
                },
            )
            .attachTo(destroyer)
    }

    private fun showCurlExportDialog(name: String, command: CurlCommand) {
        dialogState = getCurlExportDialog(name, command)
    }

    fun onExportAsFileOptionSelected(shortcutId: String) {
        showFileExportDialog(shortcutId)
    }

    private fun showFileExportDialog(shortcutId: String) {
        val shortcut = getShortcutById(shortcutId) ?: return
        emitEvent(
            ShortcutListEvent.ShowFileExportDialog(
                shortcutId,
                format = getPreferredExportFormat(),
                variableIds = getVariableIdsRequiredForExport(shortcut),
            )
        )
    }

    fun onExportDestinationSelected(uri: Uri) {
        val shortcut = exportingShortcutId?.let(::getShortcutById) ?: return
        exportingShortcutId = null
        emitEvent(
            ShortcutListEvent.StartExport(
                shortcutId = shortcut.id,
                uri = uri,
                format = getPreferredExportFormat(),
                variableIds = getVariableIdsRequiredForExport(shortcut),
            )
        )
    }

    private fun getPreferredExportFormat() =
        if (settings.useLegacyExportFormat) ExportFormat.LEGACY_JSON else ExportFormat.ZIP

    fun onMoveToCategoryOptionSelected(shortcutId: String) {
        dialogState = getMoveToCategoryDialog(
            shortcutId,
            categoryOptions = categories
                .filter { it.id != category.id }
                .map { category ->
                    GetMoveToCategoryDialogUseCase.CategoryOption(category.id, category.name)
                },
            viewModel = this,
        )
    }

    fun onMoveTargetCategorySelected(shortcutId: String, categoryId: String) {
        val shortcut = getShortcutById(shortcutId) ?: return
        performOperation(
            shortcutRepository.moveShortcutToCategory(shortcutId, categoryId)
        ) {
            showSnackbar(StringResLocalizable(R.string.shortcut_moved, shortcut.name))
        }
    }

    fun onShortcutEdited() {
        logInfo("Shortcut editing completed")
        eventBridge.submit(ChildViewModelEvent.ShortcutEdited)
    }

    fun onFileExportStarted(shortcutId: String) {
        exportingShortcutId = shortcutId
    }

    private fun getVariableIdsRequiredForExport(shortcut: ShortcutModel) =
        // TODO: Recursively collect variables referenced by other variables
        VariableResolver.extractVariableIds(
            shortcut,
            variableLookup = VariableManager(variables),
        )

    fun onDeletionConfirmed(shortcutId: String) {
        val shortcut = getShortcutById(shortcutId) ?: return
        performOperation(
            shortcutRepository.deleteShortcut(shortcutId)
                .mergeWith(pendingExecutionsRepository.removePendingExecutionsForShortcut(shortcutId))
                .andThen(widgetsRepository.deleteDeadWidgets())
        ) {
            showSnackbar(StringResLocalizable(R.string.shortcut_deleted, shortcut.name))
            eventBridge.submit(ChildViewModelEvent.RemoveShortcutFromHomeScreen(shortcut.toLauncherShortcut()))
        }
    }

    data class InitData(
        val categoryId: String,
        val selectionMode: SelectionMode,
    )
}
