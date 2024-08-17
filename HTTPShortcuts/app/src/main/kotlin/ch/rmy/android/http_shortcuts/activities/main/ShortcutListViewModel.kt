package ch.rmy.android.http_shortcuts.activities.main

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.truncate
import ch.rmy.android.framework.ui.IntentBuilder
import ch.rmy.android.framework.utils.ClipboardUtil
import ch.rmy.android.framework.utils.FileUtil
import ch.rmy.android.framework.utils.localization.QuantityStringLocalizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.ViewModelScope
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogHandler
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogState
import ch.rmy.android.http_shortcuts.activities.execute.ExecutionStarter
import ch.rmy.android.http_shortcuts.activities.main.models.ShortcutItem
import ch.rmy.android.http_shortcuts.activities.main.usecases.SecondaryLauncherMapperUseCase
import ch.rmy.android.http_shortcuts.activities.variables.usecases.GetUsedVariableIdsUseCase
import ch.rmy.android.http_shortcuts.data.domains.app.AppRepository
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryRepository
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.PendingExecutionsRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.domains.widgets.WidgetsRepository
import ch.rmy.android.http_shortcuts.data.enums.PendingExecutionType
import ch.rmy.android.http_shortcuts.data.enums.SelectionMode
import ch.rmy.android.http_shortcuts.data.enums.ShortcutClickBehavior
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.PendingExecution
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.extensions.toShortcutPlaceholder
import ch.rmy.android.http_shortcuts.extensions.type
import ch.rmy.android.http_shortcuts.import_export.CurlExporter
import ch.rmy.android.http_shortcuts.import_export.ExportFormat
import ch.rmy.android.http_shortcuts.import_export.Exporter
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import ch.rmy.android.http_shortcuts.scheduling.AlarmScheduler
import ch.rmy.android.http_shortcuts.scheduling.ExecutionScheduler
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutUpdater
import ch.rmy.android.http_shortcuts.utils.SecondaryLauncherManager
import ch.rmy.android.http_shortcuts.utils.Settings
import ch.rmy.android.http_shortcuts.utils.ShareUtil
import ch.rmy.curlcommand.CurlConstructor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ShortcutListViewModel
@Inject
constructor(
    application: Application,
    private val appRepository: AppRepository,
    private val shortcutRepository: ShortcutRepository,
    private val categoryRepository: CategoryRepository,
    private val variableRepository: VariableRepository,
    private val pendingExecutionsRepository: PendingExecutionsRepository,
    private val widgetsRepository: WidgetsRepository,
    private val curlExporter: CurlExporter,
    private val executionScheduler: ExecutionScheduler,
    private val settings: Settings,
    private val exporter: Exporter,
    private val getUsedVariableIds: GetUsedVariableIdsUseCase,
    private val launcherShortcutManager: LauncherShortcutManager,
    private val launcherShortcutUpdater: LauncherShortcutUpdater,
    private val secondaryLauncherMapper: SecondaryLauncherMapperUseCase,
    private val secondaryLauncherManager: SecondaryLauncherManager,
    private val alarmScheduler: AlarmScheduler,
    private val activityProvider: ActivityProvider,
    private val clipboardUtil: ClipboardUtil,
    private val dialogHandler: ExecuteDialogHandler,
    private val shareUtil: ShareUtil,
    private val executionStarter: ExecutionStarter,
) : BaseViewModel<ShortcutListViewModel.InitData, ShortcutListViewState>(application) {

    private lateinit var category: Category
    private var variables: List<Variable> = emptyList()
    private var pendingShortcuts: List<PendingExecution> = emptyList()

    private var activeShortcutId: ShortcutId? = null

    private var currentJob: Job? = null
        set(value) {
            field?.cancel()
            field = value
        }

    override suspend fun initialize(data: InitData): ShortcutListViewState {
        val categoriesFlow = categoryRepository.getObservableCategory(data.categoryId)
        category = categoriesFlow.first()

        viewModelScope.launch {
            categoriesFlow.collect {
                category = it
                recomputeShortcutList()
            }
        }
        viewModelScope.launch {
            variableRepository.getObservableVariables()
                .collect { variables ->
                    this@ShortcutListViewModel.variables = variables
                }
        }
        viewModelScope.launch {
            pendingExecutionsRepository.getObservablePendingExecutions()
                .collect { pendingShortcuts ->
                    this@ShortcutListViewModel.pendingShortcuts = pendingShortcuts
                    recomputeShortcutList()
                }
        }

        val appLockFlow = appRepository.getObservableLock()
        val isAppLocked = appLockFlow.first() != null
        viewModelScope.launch {
            appLockFlow.collect { appLock ->
                updateViewState {
                    copy(isAppLocked = appLock != null)
                }
            }
        }
        return ShortcutListViewState(
            isAppLocked = isAppLocked,
            shortcuts = mapShortcuts(),
            background = category.categoryBackgroundType,
        )
    }

    override suspend fun onReactivated() {
        recomputeShortcutList()
    }

    private suspend fun recomputeShortcutList() {
        updateViewState {
            copy(shortcuts = mapShortcuts())
        }
    }

    private fun mapShortcuts(): List<ShortcutItem> {
        val includeHidden = settings.showHiddenShortcuts
        return category.shortcuts
            .mapNotNull { shortcut ->
                if (!includeHidden && shortcut.hidden) {
                    return@mapNotNull null
                }
                ShortcutItem(
                    id = shortcut.id,
                    name = shortcut.name,
                    description = shortcut.description,
                    icon = shortcut.icon,
                    isPending = pendingShortcuts.any { it.shortcutId == shortcut.id },
                    isHidden = shortcut.hidden,
                )
            }
    }

    private suspend fun updateLauncherSettings() {
        withContext(Dispatchers.Default) {
            launcherShortcutUpdater.updateAppShortcuts()
            val categories = categoryRepository.getCategories()
            secondaryLauncherManager.setSecondaryLauncherVisibility(secondaryLauncherMapper(categories))
        }
    }

    fun onShortcutClicked(shortcutId: ShortcutId) = runAction {
        logInfo("Shortcut clicked (selectionMode = ${initData.selectionMode}")
        if (initData.selectionMode != SelectionMode.NORMAL) {
            selectShortcut(shortcutId)
            skipAction()
        }
        if (viewState.isAppLocked) {
            executeShortcut(shortcutId)
            skipAction()
        }
        when (category.clickBehavior ?: settings.clickBehavior) {
            ShortcutClickBehavior.RUN -> executeShortcut(shortcutId)
            ShortcutClickBehavior.EDIT -> editShortcut(shortcutId)
            ShortcutClickBehavior.MENU -> showContextMenu(shortcutId)
        }
    }

    private suspend fun selectShortcut(shortcutId: ShortcutId) {
        emitEvent(ShortcutListEvent.SelectShortcut(shortcutId))
    }

    private fun executeShortcut(shortcutId: ShortcutId) {
        logInfo("Preparing to execute shortcut")
        executionStarter.execute(
            shortcutId = shortcutId,
            trigger = ShortcutTriggerType.MAIN_SCREEN,
        )
    }

    private suspend fun editShortcut(shortcutId: ShortcutId) {
        logInfo("Preparing to edit shortcut")
        navigate(
            NavigationDestination.ShortcutEditor.buildRequest(
                shortcutId = shortcutId,
                categoryId = category.id,
            )
        )
    }

    private suspend fun showContextMenu(shortcutId: ShortcutId) {
        val shortcut = getShortcutById(shortcutId) ?: return
        activeShortcutId = shortcutId
        updateDialogState(
            ShortcutListDialogState.ContextMenu(
                shortcutName = shortcut.name,
                isPending = pendingShortcuts.any { it.shortcutId == shortcut.id },
                isHidden = shortcut.hidden,
            )
        )
    }

    fun onShortcutLongClicked(shortcutId: ShortcutId) = runAction {
        if (viewState.isLongClickingEnabled) {
            showContextMenu(shortcutId)
        }
    }

    private fun getShortcutById(shortcutId: ShortcutId): Shortcut? =
        category.shortcuts.firstOrNull { it.id == shortcutId }

    fun onPlaceOnHomeScreenOptionSelected() = runAction {
        updateDialogState(null)
        val shortcutId = activeShortcutId ?: skipAction()
        val shortcut = getShortcutById(shortcutId) ?: skipAction()
        emitEvent(ShortcutListEvent.PlaceShortcutOnHomeScreen(shortcut.toShortcutPlaceholder()))
    }

    fun onExecuteOptionSelected() = runAction {
        updateDialogState(null)
        val shortcutId = activeShortcutId ?: skipAction()
        executeShortcut(shortcutId)
    }

    fun onCancelPendingExecutionOptionSelected() = runAction {
        updateDialogState(null)
        val shortcutId = activeShortcutId ?: skipAction()
        cancelPendingExecution(shortcutId)
    }

    private suspend fun cancelPendingExecution(shortcutId: ShortcutId) {
        val shortcut = getShortcutById(shortcutId) ?: return
        cancelAlarms(shortcutId)
        pendingExecutionsRepository.removePendingExecutionsForShortcut(shortcutId)
        executionScheduler.schedule()
        showSnackbar(StringResLocalizable(R.string.pending_shortcut_execution_cancelled, shortcut.name))
    }

    private suspend fun cancelAlarms(shortcutId: ShortcutId) {
        pendingExecutionsRepository.getPendingExecutionsForShortcut(shortcutId)
            .filter { it.type == PendingExecutionType.REPEAT }
            .forEach { pendingExecution ->
                alarmScheduler.cancelAlarm(pendingExecution.id, pendingExecution.requestCode)
            }
    }

    fun onEditOptionSelected() = runAction {
        updateDialogState(null)
        val shortcutId = activeShortcutId ?: skipAction()
        editShortcut(shortcutId)
    }

    fun onMoveOptionSelected() = runAction {
        updateDialogState(null)
        val shortcutId = activeShortcutId ?: skipAction()
        navigate(NavigationDestination.MoveShortcuts.buildRequest(shortcutId))
    }

    fun onDuplicateOptionSelected() = runAction {
        updateDialogState(null)
        val shortcutId = activeShortcutId ?: skipAction()
        duplicateShortcut(shortcutId)
    }

    fun onShowSelected() = runAction {
        updateDialogState(null)
        val shortcutId = activeShortcutId ?: skipAction()
        shortcutRepository.setHidden(shortcutId, false)
        showSnackbar(R.string.message_shortcut_visible)
    }

    fun onHideSelected() = runAction {
        updateDialogState(null)
        val shortcutId = activeShortcutId ?: skipAction()
        shortcutRepository.setHidden(shortcutId, true)
        showSnackbar(R.string.message_shortcut_hidden)
    }

    private suspend fun ViewModelScope<*>.duplicateShortcut(shortcutId: ShortcutId) {
        val shortcut = getShortcutById(shortcutId) ?: return
        val name = shortcut.name
        val newName = context.getString(R.string.template_shortcut_name_copy, shortcut.name)
            .truncate(Shortcut.NAME_MAX_LENGTH)
        val categoryId = category.id

        val newPosition = category.shortcuts
            .indexOfFirst { it.id == shortcut.id }
            .takeIf { it != -1 }
            ?.let { it + 1 }

        withProgressTracking {
            shortcutRepository.duplicateShortcut(shortcutId, newName, newPosition, categoryId)
            updateLauncherSettings()
            showSnackbar(StringResLocalizable(R.string.shortcut_duplicated, name))
        }
    }

    fun onDeleteOptionSelected() = runAction {
        updateDialogState(null)
        val shortcutId = activeShortcutId ?: skipAction()
        showDeletionDialog(getShortcutById(shortcutId) ?: skipAction())
    }

    private suspend fun showDeletionDialog(shortcut: Shortcut) {
        activeShortcutId = shortcut.id
        updateDialogState(
            ShortcutListDialogState.Deletion(
                shortcutName = shortcut.name,
            )
        )
    }

    fun onShowInfoOptionSelected() = runAction {
        updateDialogState(null)
        val shortcutId = activeShortcutId ?: skipAction()
        showShortcutInfoDialog(getShortcutById(shortcutId) ?: skipAction())
    }

    private suspend fun showShortcutInfoDialog(shortcut: Shortcut) {
        updateDialogState(
            ShortcutListDialogState.ShortcutInfo(
                shortcutId = shortcut.id,
                shortcutName = shortcut.name,
            )
        )
    }

    fun onExportOptionSelected() = runAction {
        val shortcutId = activeShortcutId ?: skipAction()
        val shortcut = getShortcutById(shortcutId) ?: skipAction()

        if (shortcut.type.usesUrl) {
            showExportOptionsDialog(shortcutId)
        } else {
            showFileExportDialog()
        }
    }

    private suspend fun showExportOptionsDialog(shortcutId: ShortcutId) {
        activeShortcutId = shortcutId
        updateDialogState(
            ShortcutListDialogState.ExportOptions,
        )
    }

    fun onExportAsCurlOptionSelected() = runAction {
        updateDialogState(null)
        val shortcutId = activeShortcutId ?: skipAction()
        val shortcut = getShortcutById(shortcutId) ?: skipAction()
        try {
            val command = curlExporter.generateCommand(shortcut, dialogHandler)
                .let(CurlConstructor::toCurlCommandString)
            updateDialogState(
                ShortcutListDialogState.CurlExport(shortcut.name, command)
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            showToast(R.string.error_generic)
            logException(e)
        }
    }

    fun onCurlExportCopyButtonClicked() = runAction {
        val curlCommand = (viewState.dialogState as? ShortcutListDialogState.CurlExport)?.command ?: skipAction()
        updateDialogState(null)
        clipboardUtil.copyToClipboard(curlCommand)
    }

    fun onCurlExportShareButtonClicked() = runAction {
        val curlCommand = (viewState.dialogState as? ShortcutListDialogState.CurlExport)?.command ?: skipAction()
        updateDialogState(null)
        activityProvider.withActivity { activity ->
            shareUtil.shareText(activity, curlCommand)
        }
    }

    fun onExportAsFileOptionSelected() = runAction {
        showFileExportDialog()
    }

    private suspend fun showFileExportDialog() {
        updateDialogState(
            ShortcutListDialogState.ExportDestinationOptions,
        )
    }

    fun onExportToFileOptionSelected() = runAction {
        updateDialogState(null)
        emitEvent(ShortcutListEvent.OpenFilePickerForExport)
    }

    fun onExportViaSharingOptionSelected() = runAction {
        updateDialogState(null)
        sendExport()
    }

    fun onFilePickedForExport(file: Uri) = runAction {
        val shortcut = activeShortcutId?.let(::getShortcutById) ?: skipAction()

        currentJob = launch {
            updateDialogState(ShortcutListDialogState.ExportProgress)
            try {
                val status = try {
                    val variableIds = getUsedVariableIds(shortcut.id)
                    exporter.exportToUri(
                        file,
                        excludeDefaults = true,
                        shortcutIds = setOf(shortcut.id),
                        variableIds = variableIds,
                    )
                } finally {
                    hideExportProgressDialog()
                }
                showSnackbar(
                    QuantityStringLocalizable(
                        R.plurals.shortcut_export_success,
                        status.exportedShortcuts,
                        status.exportedShortcuts,
                    )
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logException(e)
                updateDialogState(
                    ShortcutListDialogState.ExportError(
                        e.message.orEmpty()
                    )
                )
            }
        }
    }

    private fun sendExport() {
        val shortcut = activeShortcutId?.let(::getShortcutById) ?: return

        currentJob = viewModelScope.launch {
            updateDialogState(ShortcutListDialogState.ExportProgress)
            try {
                val cacheFile = FileUtil.createCacheFile(context, ExportFormat.ZIP.getFileName(single = true))

                exporter.exportToUri(
                    cacheFile,
                    excludeDefaults = true,
                    shortcutIds = setOf(shortcut.id),
                    variableIds = getUsedVariableIds(shortcut.id),
                )

                sendIntent(object : IntentBuilder {
                    override fun build(context: Context) =
                        Intent(Intent.ACTION_SEND)
                            .setType(ExportFormat.ZIP.fileTypeForSharing)
                            .putExtra(Intent.EXTRA_STREAM, cacheFile)
                            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            .let {
                                Intent.createChooser(it, context.getString(R.string.title_export))
                            }
                })
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleUnexpectedError(e)
            } finally {
                hideExportProgressDialog()
            }
        }
    }

    private suspend fun hideExportProgressDialog() {
        if (getCurrentViewState().dialogState is ShortcutListDialogState.ExportProgress) {
            updateDialogState(null)
        }
    }

    fun onDeletionConfirmed() = runAction {
        updateDialogState(null)
        val shortcutId = activeShortcutId ?: skipAction()
        val shortcut = getShortcutById(shortcutId) ?: skipAction()
        withProgressTracking {
            shortcutRepository.deleteShortcut(shortcutId)
            pendingExecutionsRepository.removePendingExecutionsForShortcut(shortcutId)
            cancelAlarms(shortcutId)
            widgetsRepository.deleteDeadWidgets()
            launcherShortcutManager.removeShortcut(shortcutId)
            showSnackbar(StringResLocalizable(R.string.shortcut_deleted, shortcut.name))
            emitEvent(ShortcutListEvent.RemoveShortcutFromHomeScreen(shortcut.toShortcutPlaceholder()))
        }
    }

    fun onDialogDismissed() = runAction {
        currentJob = null
        updateDialogState(null)
    }

    private suspend fun updateDialogState(dialogState: ShortcutListDialogState?) {
        updateViewState {
            copy(dialogState = dialogState)
        }
    }

    val executeDialogState: StateFlow<ExecuteDialogState<*>?>
        get() = dialogHandler.dialogState

    fun onExecuteDialogDismissed() {
        dialogHandler.onDialogDismissed()
    }

    fun onExecuteDialogResult(result: Any) {
        dialogHandler.onDialogResult(result)
    }

    data class InitData(
        val categoryId: CategoryId,
        val selectionMode: SelectionMode,
    )
}
