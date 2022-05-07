package ch.rmy.android.http_shortcuts.activities.main

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.swapped
import ch.rmy.android.framework.extensions.toLocalizable
import ch.rmy.android.framework.extensions.truncate
import ch.rmy.android.framework.ui.IntentBuilder
import ch.rmy.android.framework.utils.localization.QuantityStringLocalizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.EventBridge
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.framework.viewmodel.viewstate.ProgressDialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.activities.main.usecases.GetContextMenuDialogUseCase
import ch.rmy.android.http_shortcuts.activities.main.usecases.GetCurlExportDialogUseCase
import ch.rmy.android.http_shortcuts.activities.main.usecases.GetExportOptionsDialogUseCase
import ch.rmy.android.http_shortcuts.activities.main.usecases.GetMoveOptionsDialogUseCase
import ch.rmy.android.http_shortcuts.activities.main.usecases.GetMoveToCategoryDialogUseCase
import ch.rmy.android.http_shortcuts.activities.main.usecases.GetShortcutDeletionDialogUseCase
import ch.rmy.android.http_shortcuts.activities.main.usecases.GetShortcutInfoDialogUseCase
import ch.rmy.android.http_shortcuts.activities.variables.usecases.GetUsedVariableIdsUseCase
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.app.AppRepository
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryRepository
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.PendingExecutionsRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.domains.widgets.WidgetsRepository
import ch.rmy.android.http_shortcuts.data.enums.CategoryBackgroundType
import ch.rmy.android.http_shortcuts.data.enums.SelectionMode
import ch.rmy.android.http_shortcuts.data.enums.ShortcutClickBehavior
import ch.rmy.android.http_shortcuts.data.models.CategoryModel
import ch.rmy.android.http_shortcuts.data.models.PendingExecutionModel
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.data.models.VariableModel
import ch.rmy.android.http_shortcuts.exceptions.CanceledByUserException
import ch.rmy.android.http_shortcuts.extensions.toLauncherShortcut
import ch.rmy.android.http_shortcuts.extensions.type
import ch.rmy.android.http_shortcuts.import_export.CurlExporter
import ch.rmy.android.http_shortcuts.import_export.ExportFormat
import ch.rmy.android.http_shortcuts.import_export.Exporter
import ch.rmy.android.http_shortcuts.scheduling.ExecutionScheduler
import ch.rmy.android.http_shortcuts.usecases.GetExportDestinationOptionsDialogUseCase
import ch.rmy.android.http_shortcuts.utils.FileUtil
import ch.rmy.android.http_shortcuts.utils.Settings
import ch.rmy.curlcommand.CurlCommand
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class ShortcutListViewModel(
    application: Application,
) : BaseViewModel<ShortcutListViewModel.InitData, ShortcutListViewState>(application), WithDialog {

    @Inject
    lateinit var appRepository: AppRepository

    @Inject
    lateinit var shortcutRepository: ShortcutRepository

    @Inject
    lateinit var categoryRepository: CategoryRepository

    @Inject
    lateinit var variableRepository: VariableRepository

    @Inject
    lateinit var pendingExecutionsRepository: PendingExecutionsRepository

    @Inject
    lateinit var widgetsRepository: WidgetsRepository

    @Inject
    lateinit var curlExporter: CurlExporter

    @Inject
    lateinit var executionScheduler: ExecutionScheduler

    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var getCurlExportDialog: GetCurlExportDialogUseCase

    @Inject
    lateinit var getShortcutInfoDialog: GetShortcutInfoDialogUseCase

    @Inject
    lateinit var getShortcutDeletionDialog: GetShortcutDeletionDialogUseCase

    @Inject
    lateinit var getExportOptionsDialog: GetExportOptionsDialogUseCase

    @Inject
    lateinit var getExportDestinationOptionsDialog: GetExportDestinationOptionsDialogUseCase

    @Inject
    lateinit var getMoveOptionsDialog: GetMoveOptionsDialogUseCase

    @Inject
    lateinit var getContextMenuDialog: GetContextMenuDialogUseCase

    @Inject
    lateinit var getMoveToCategoryDialog: GetMoveToCategoryDialogUseCase

    @Inject
    lateinit var exporter: Exporter

    @Inject
    lateinit var getUsedVariableIds: GetUsedVariableIdsUseCase

    private val eventBridge = EventBridge(ChildViewModelEvent::class.java)

    init {
        getApplicationComponent().inject(this)
    }

    private lateinit var category: CategoryModel
    private var categories: List<CategoryModel> = emptyList()
    private var variables: List<VariableModel> = emptyList()
    private var pendingShortcuts: List<PendingExecutionModel> = emptyList()

    private var exportingShortcutId: ShortcutId? = null
    private var isAppLocked = false

    private var disposable: Disposable? = null

    override var dialogState: DialogState?
        get() = currentViewState?.dialogState
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
                is CategoryBackgroundType.Default -> ShortcutListItem.TextColor.DARK
                is CategoryBackgroundType.Color -> ShortcutListItem.TextColor.BRIGHT
                is CategoryBackgroundType.Wallpaper -> ShortcutListItem.TextColor.BRIGHT
            }
            shortcuts.map { shortcut ->
                ShortcutListItem.Shortcut(
                    id = shortcut.id,
                    name = shortcut.name,
                    description = shortcut.description,
                    icon = shortcut.icon,
                    isPending = pendingShortcuts.any { it.shortcutId == shortcut.id },
                    textColor = textColor,
                    useTextShadow = category.categoryBackgroundType.useTextShadow,
                )
            }
        }
    }

    fun onPaused() {
        if (isInitialized && currentViewState!!.isInMovingMode) {
            disableMovingMode()
        }
    }

    fun onBackPressed(): Boolean =
        if (isInitialized && currentViewState!!.isInMovingMode) {
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

    fun onShortcutMoved(shortcutId1: ShortcutId, shortcutId2: ShortcutId) {
        updateViewState {
            copy(shortcuts = shortcuts.swapped(shortcutId1, shortcutId2) { (this as? ShortcutListItem.Shortcut)?.id })
        }
        performOperation(
            shortcutRepository.swapShortcutPositions(category.id, shortcutId1, shortcutId2)
        )
    }

    fun onShortcutClicked(shortcutId: ShortcutId) {
        doWithViewState { viewState ->
            logInfo("Shortcut clicked")
            if (viewState.isInMovingMode) {
                showSnackbar(R.string.message_moving_enabled)
                return@doWithViewState
            }
            if (initData.selectionMode != SelectionMode.NORMAL) {
                selectShortcut(shortcutId)
                return@doWithViewState
            }
            if (viewState.isAppLocked) {
                executeShortcut(shortcutId)
                return@doWithViewState
            }
            when (category.clickBehavior ?: settings.clickBehavior) {
                ShortcutClickBehavior.RUN -> executeShortcut(shortcutId)
                ShortcutClickBehavior.EDIT -> editShortcut(shortcutId)
                ShortcutClickBehavior.MENU -> showContextMenu(shortcutId)
            }
        }
    }

    private fun selectShortcut(shortcutId: ShortcutId) {
        eventBridge.submit(ChildViewModelEvent.SelectShortcut(shortcutId))
    }

    private fun executeShortcut(shortcutId: ShortcutId) {
        logInfo("Preparing to execute shortcut")
        openActivity(ExecuteActivity.IntentBuilder(shortcutId))
    }

    private fun editShortcut(shortcutId: ShortcutId) {
        logInfo("Preparing to edit shortcut")
        emitEvent(
            ShortcutListEvent.OpenShortcutEditor(
                shortcutId = shortcutId,
                categoryId = category.id,
            )
        )
    }

    private fun showContextMenu(shortcutId: ShortcutId) {
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

    fun onShortcutLongClicked(shortcutId: ShortcutId) {
        doWithViewState { viewState ->
            if (viewState.isLongClickingEnabled) {
                showContextMenu(shortcutId)
            }
        }
    }

    private fun getShortcutById(shortcutId: ShortcutId): ShortcutModel? =
        category.shortcuts.firstOrNull { it.id == shortcutId }

    fun onPlaceOnHomeScreenOptionSelected(shortcutId: ShortcutId) {
        val shortcut = getShortcutById(shortcutId) ?: return
        eventBridge.submit(ChildViewModelEvent.PlaceShortcutOnHomeScreen(shortcut.toLauncherShortcut()))
    }

    fun onExecuteOptionSelected(shortcutId: ShortcutId) {
        executeShortcut(shortcutId)
    }

    fun onCancelPendingExecutionOptionSelected(shortcutId: ShortcutId) {
        cancelPendingExecution(shortcutId)
    }

    private fun cancelPendingExecution(shortcutId: ShortcutId) {
        val shortcut = getShortcutById(shortcutId) ?: return
        pendingExecutionsRepository.removePendingExecutionsForShortcut(shortcutId)
            .andThen(executionScheduler.schedule())
            .subscribe {
                showSnackbar(StringResLocalizable(R.string.pending_shortcut_execution_cancelled, shortcut.name))
            }
            .attachTo(destroyer)
    }

    fun onEditOptionSelected(shortcutId: ShortcutId) {
        editShortcut(shortcutId)
    }

    fun onMoveOptionSelected(shortcutId: ShortcutId) {
        val canMoveWithinCategory = canMoveWithinCategory()
        val canMoveAcrossCategories = canMoveAcrossCategories()
        when {
            canMoveWithinCategory && canMoveAcrossCategories -> showMoveOptionsDialog(shortcutId)
            canMoveWithinCategory -> enableMovingMode()
            canMoveAcrossCategories -> onMoveToCategoryOptionSelected(shortcutId)
        }
    }

    private fun showMoveOptionsDialog(shortcutId: ShortcutId) {
        dialogState = getMoveOptionsDialog(shortcutId, this)
    }

    fun onDuplicateOptionSelected(shortcutId: ShortcutId) {
        duplicateShortcut(shortcutId)
    }

    private fun duplicateShortcut(shortcutId: ShortcutId) {
        val shortcut = getShortcutById(shortcutId) ?: return
        val name = shortcut.name
        val newName = context.getString(R.string.template_shortcut_name_copy, shortcut.name)
            .truncate(ShortcutModel.NAME_MAX_LENGTH)
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

    fun onDeleteOptionSelected(shortcutId: ShortcutId) {
        showDeletionDialog(getShortcutById(shortcutId) ?: return)
    }

    private fun showDeletionDialog(shortcut: ShortcutModel) {
        dialogState = getShortcutDeletionDialog(shortcut.id, shortcut.name.toLocalizable(), this)
    }

    fun onShowInfoOptionSelected(shortcutId: ShortcutId) {
        showShortcutInfoDialog(getShortcutById(shortcutId) ?: return)
    }

    private fun showShortcutInfoDialog(shortcut: ShortcutModel) {
        dialogState = getShortcutInfoDialog(shortcut.id, shortcut.name)
    }

    fun onExportOptionSelected(shortcutId: ShortcutId) {
        val shortcut = getShortcutById(shortcutId) ?: return

        if (shortcut.type.usesUrl) {
            showExportOptionsDialog(shortcutId)
        } else {
            showFileExportDialog(shortcutId)
        }
    }

    private fun showExportOptionsDialog(shortcutId: ShortcutId) {
        dialogState = getExportOptionsDialog(shortcutId, this)
    }

    fun onExportAsCurlOptionSelected(shortcutId: ShortcutId) {
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

    fun onExportAsFileOptionSelected(shortcutId: ShortcutId) {
        showFileExportDialog(shortcutId)
    }

    private fun showFileExportDialog(shortcutId: ShortcutId) {
        exportingShortcutId = shortcutId
        dialogState = getExportDestinationOptionsDialog(
            onExportToFileOptionSelected = {
                emitEvent(ShortcutListEvent.OpenFilePickerForExport(getExportFormat()))
            },
            onExportViaSharingOptionSelected = {
                sendExport()
            },
        )
    }

    fun onFilePickedForExport(file: Uri) {
        val shortcut = exportingShortcutId?.let(::getShortcutById) ?: return
        getUsedVariableIds(shortcut.id)
            .flatMap { variableIds ->
                exporter.exportToUri(
                    file,
                    format = getExportFormat(),
                    excludeDefaults = true,
                    shortcutId = shortcut.id,
                    variableIds = variableIds,
                )
            }
            .doOnSubscribe {
                showProgressDialog(R.string.export_in_progress)
            }
            .doFinally {
                hideProgressDialog()
            }
            .subscribe(
                { status ->
                    showSnackbar(
                        QuantityStringLocalizable(
                            R.plurals.shortcut_export_success,
                            status.exportedShortcuts,
                            status.exportedShortcuts,
                        )
                    )
                },
                { error ->
                    logException(error)
                    dialogState = DialogState.create {
                        message(context.getString(R.string.export_failed_with_reason, error.message))
                            .positive(R.string.dialog_ok)
                            .build()
                    }
                },
            )
            .also {
                disposable = it
            }
            .attachTo(destroyer)
    }

    private fun sendExport() {
        val shortcut = exportingShortcutId?.let(::getShortcutById) ?: return
        val format = getExportFormat()
        val cacheFile = FileUtil.createCacheFile(context, format.getFileName(single = true))

        getUsedVariableIds(shortcut.id)
            .flatMap { variableIds ->
                exporter
                    .exportToUri(
                        cacheFile,
                        excludeDefaults = true,
                        shortcutId = shortcut.id,
                        variableIds = variableIds,
                    )
            }
            .doOnSubscribe {
                showProgressDialog(R.string.export_in_progress)
            }
            .doFinally {
                hideProgressDialog()
            }
            .subscribe(
                {
                    openActivity(object : IntentBuilder {
                        override fun build(context: Context) =
                            Intent(Intent.ACTION_SEND)
                                .setType(format.fileTypeForSharing)
                                .putExtra(Intent.EXTRA_STREAM, cacheFile)
                                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                .let {
                                    Intent.createChooser(it, context.getString(R.string.title_export))
                                }
                    })
                },
                ::handleUnexpectedError,
            )
            .also {
                disposable = it
            }
            .attachTo(destroyer)
    }

    private fun showProgressDialog(message: Int) {
        dialogState = ProgressDialogState(StringResLocalizable(message), ::onProgressDialogCanceled)
    }

    private fun hideProgressDialog() {
        if (dialogState?.id == ProgressDialogState.DIALOG_ID) {
            dialogState = null
        }
    }

    private fun onProgressDialogCanceled() {
        disposable?.dispose()
    }

    private fun getExportFormat() =
        if (settings.useLegacyExportFormat) ExportFormat.LEGACY_JSON else ExportFormat.ZIP

    fun onMoveToCategoryOptionSelected(shortcutId: ShortcutId) {
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

    fun onMoveTargetCategorySelected(shortcutId: ShortcutId, categoryId: CategoryId) {
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

    fun onDeletionConfirmed(shortcutId: ShortcutId) {
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
        val categoryId: CategoryId,
        val selectionMode: SelectionMode,
    )
}
