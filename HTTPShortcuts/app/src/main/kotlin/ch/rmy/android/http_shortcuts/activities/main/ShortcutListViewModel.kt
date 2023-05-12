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
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.activities.main.models.ShortcutItem
import ch.rmy.android.http_shortcuts.activities.main.usecases.LauncherShortcutMapperUseCase
import ch.rmy.android.http_shortcuts.activities.main.usecases.SecondaryLauncherMapperUseCase
import ch.rmy.android.http_shortcuts.activities.moving.MoveActivity
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
import ch.rmy.android.http_shortcuts.scheduling.AlarmScheduler
import ch.rmy.android.http_shortcuts.scheduling.ExecutionScheduler
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import ch.rmy.android.http_shortcuts.utils.SecondaryLauncherManager
import ch.rmy.android.http_shortcuts.utils.Settings
import ch.rmy.android.http_shortcuts.utils.ShareUtil
import ch.rmy.curlcommand.CurlConstructor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class ShortcutListViewModel(
    application: Application,
) : BaseViewModel<ShortcutListViewModel.InitData, ShortcutListViewState>(application) {

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
    lateinit var exporter: Exporter

    @Inject
    lateinit var getUsedVariableIds: GetUsedVariableIdsUseCase

    @Inject
    lateinit var launcherShortcutMapper: LauncherShortcutMapperUseCase

    @Inject
    lateinit var secondaryLauncherMapper: SecondaryLauncherMapperUseCase

    @Inject
    lateinit var launcherShortcutManager: LauncherShortcutManager

    @Inject
    lateinit var secondaryLauncherManager: SecondaryLauncherManager

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    @Inject
    lateinit var activityProvider: ActivityProvider

    @Inject
    lateinit var clipboardUtil: ClipboardUtil

    init {
        getApplicationComponent().inject(this)
    }

    private lateinit var category: Category
    private var variables: List<Variable> = emptyList()
    private var pendingShortcuts: List<PendingExecution> = emptyList()

    private var isAppLocked = false

    private var activeShortcutId: ShortcutId? = null

    private var currentJob: Job? = null
        set(value) {
            field?.cancel()
            field = value
        }

    override fun onInitializationStarted(data: InitData) {
        viewModelScope.launch {
            categoryRepository.getObservableCategory(data.categoryId)
                .collect { category ->
                    this@ShortcutListViewModel.category = category
                    if (isInitialized) {
                        recomputeShortcutList()
                    } else {
                        finalizeInitialization()
                    }
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
                    if (isInitialized) {
                        recomputeShortcutList()
                    }
                }
        }

        viewModelScope.launch {
            appRepository.getObservableLock().collect { appLock ->
                isAppLocked = appLock != null
                if (isInitialized) {
                    updateViewState {
                        copy(isAppLocked = this@ShortcutListViewModel.isAppLocked)
                    }
                }
            }
        }
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

    private fun mapShortcuts(): List<ShortcutItem> =
        category.shortcuts.map { shortcut ->
            ShortcutItem(
                id = shortcut.id,
                name = shortcut.name,
                description = shortcut.description,
                icon = shortcut.icon,
                isPending = pendingShortcuts.any { it.shortcutId == shortcut.id },
            )
        }

    private fun updateLauncherSettings() {
        viewModelScope.launch {
            val categories = categoryRepository.getCategories()
            launcherShortcutManager.updateAppShortcuts(launcherShortcutMapper(categories))
            secondaryLauncherManager.setSecondaryLauncherVisibility(secondaryLauncherMapper(categories))
        }
    }

    fun onShortcutClicked(shortcutId: ShortcutId) {
        doWithViewState { viewState ->
            logInfo("Shortcut clicked")
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
        emitEvent(ShortcutListEvent.SelectShortcut(shortcutId))
    }

    private fun executeShortcut(shortcutId: ShortcutId) {
        logInfo("Preparing to execute shortcut")
        openActivity(ExecuteActivity.IntentBuilder(shortcutId).trigger(ShortcutTriggerType.MAIN_SCREEN))
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
        activeShortcutId = shortcutId
        updateDialogState(
            ShortcutListDialogState.ContextMenu(
                shortcutName = shortcut.name,
                isPending = pendingShortcuts.any { it.shortcutId == shortcut.id },
            )
        )
    }

    fun onShortcutLongClicked(shortcutId: ShortcutId) {
        doWithViewState { viewState ->
            if (viewState.isLongClickingEnabled) {
                showContextMenu(shortcutId)
            }
        }
    }

    private fun getShortcutById(shortcutId: ShortcutId): Shortcut? =
        category.shortcuts.firstOrNull { it.id == shortcutId }

    fun onPlaceOnHomeScreenOptionSelected() {
        updateDialogState(null)
        val shortcutId = activeShortcutId ?: return
        val shortcut = getShortcutById(shortcutId) ?: return
        emitEvent(ShortcutListEvent.PlaceShortcutOnHomeScreen(shortcut.toShortcutPlaceholder()))
    }

    fun onExecuteOptionSelected() {
        updateDialogState(null)
        val shortcutId = activeShortcutId ?: return
        executeShortcut(shortcutId)
    }

    fun onCancelPendingExecutionOptionSelected() {
        updateDialogState(null)
        val shortcutId = activeShortcutId ?: return
        cancelPendingExecution(shortcutId)
    }

    private fun cancelPendingExecution(shortcutId: ShortcutId) {
        val shortcut = getShortcutById(shortcutId) ?: return
        viewModelScope.launch {
            cancelAlarms(shortcutId)
            pendingExecutionsRepository.removePendingExecutionsForShortcut(shortcutId)
            executionScheduler.schedule()
            showSnackbar(StringResLocalizable(R.string.pending_shortcut_execution_cancelled, shortcut.name))
        }
    }

    private suspend fun cancelAlarms(shortcutId: ShortcutId) {
        pendingExecutionsRepository.getPendingExecutionsForShortcut(shortcutId)
            .filter { it.type == PendingExecutionType.REPEAT }
            .forEach { pendingExecution ->
                alarmScheduler.cancelAlarm(pendingExecution.id, pendingExecution.requestCode)
            }
    }

    fun onEditOptionSelected() {
        updateDialogState(null)
        val shortcutId = activeShortcutId ?: return
        editShortcut(shortcutId)
    }

    fun onMoveOptionSelected() {
        updateDialogState(null)
        openActivity(
            MoveActivity.IntentBuilder()
        )
    }

    fun onDuplicateOptionSelected() {
        updateDialogState(null)
        val shortcutId = activeShortcutId ?: return
        duplicateShortcut(shortcutId)
    }

    private fun duplicateShortcut(shortcutId: ShortcutId) {
        val shortcut = getShortcutById(shortcutId) ?: return
        val name = shortcut.name
        val newName = context.getString(R.string.template_shortcut_name_copy, shortcut.name)
            .truncate(Shortcut.NAME_MAX_LENGTH)
        val categoryId = category.id

        val newPosition = category.shortcuts
            .indexOfFirst { it.id == shortcut.id }
            .takeIf { it != -1 }
            ?.let { it + 1 }

        launchWithProgressTracking {
            shortcutRepository.duplicateShortcut(shortcutId, newName, newPosition, categoryId)
            updateLauncherSettings()
            showSnackbar(StringResLocalizable(R.string.shortcut_duplicated, name))
        }
    }

    fun onDeleteOptionSelected() {
        updateDialogState(null)
        val shortcutId = activeShortcutId ?: return
        showDeletionDialog(getShortcutById(shortcutId) ?: return)
    }

    private fun showDeletionDialog(shortcut: Shortcut) {
        activeShortcutId = shortcut.id
        updateDialogState(
            ShortcutListDialogState.Deletion(
                shortcutName = shortcut.name,
            )
        )
    }

    fun onShowInfoOptionSelected() {
        updateDialogState(null)
        val shortcutId = activeShortcutId ?: return
        showShortcutInfoDialog(getShortcutById(shortcutId) ?: return)
    }

    private fun showShortcutInfoDialog(shortcut: Shortcut) {
        updateDialogState(
            ShortcutListDialogState.ShortcutInfo(
                shortcutId = shortcut.id,
                shortcutName = shortcut.name,
            )
        )
    }

    fun onExportOptionSelected() {
        val shortcutId = activeShortcutId ?: return
        val shortcut = getShortcutById(shortcutId) ?: return

        if (shortcut.type.usesUrl) {
            showExportOptionsDialog(shortcutId)
        } else {
            showFileExportDialog()
        }
    }

    private fun showExportOptionsDialog(shortcutId: ShortcutId) {
        activeShortcutId = shortcutId
        updateDialogState(
            ShortcutListDialogState.ExportOptions,
        )
    }

    fun onExportAsCurlOptionSelected() {
        updateDialogState(null)
        val shortcutId = activeShortcutId ?: return
        val shortcut = getShortcutById(shortcutId) ?: return
        viewModelScope.launch {
            try {
                val command = curlExporter.generateCommand(shortcut)
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
    }

    fun onCurlExportCopyButtonClicked() {
        val curlCommand = (currentViewState?.dialogState as? ShortcutListDialogState.CurlExport)?.command ?: return
        updateDialogState(null)
        clipboardUtil.copyToClipboard(curlCommand)
    }

    fun onCurlExportShareButtonClicked() {
        val curlCommand = (currentViewState?.dialogState as? ShortcutListDialogState.CurlExport)?.command ?: return
        updateDialogState(null)
        ShareUtil.shareText(activityProvider.getActivity(), curlCommand)
    }

    fun onExportAsFileOptionSelected() {
        showFileExportDialog()
    }

    private fun showFileExportDialog() {
        updateDialogState(
            ShortcutListDialogState.ExportDestinationOptions,
        )
    }

    fun onExportToFileOptionSelected() {
        updateDialogState(null)
        emitEvent(ShortcutListEvent.OpenFilePickerForExport(getExportFormat()))
    }

    fun onExportViaSharingOptionSelected() {
        updateDialogState(null)
        sendExport()
    }

    fun onFilePickedForExport(file: Uri) {
        val shortcut = activeShortcutId?.let(::getShortcutById) ?: return

        currentJob = viewModelScope.launch {
            updateDialogState(ShortcutListDialogState.ExportProgress)
            try {
                val status = try {
                    val variableIds = getUsedVariableIds(shortcut.id)
                    exporter.exportToUri(
                        file,
                        format = getExportFormat(),
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
                val format = getExportFormat()
                val cacheFile = FileUtil.createCacheFile(context, format.getFileName(single = true))

                exporter.exportToUri(
                    cacheFile,
                    excludeDefaults = true,
                    shortcutIds = setOf(shortcut.id),
                    variableIds = getUsedVariableIds(shortcut.id),
                )

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
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleUnexpectedError(e)
            } finally {
                hideExportProgressDialog()
            }
        }
    }

    private fun hideExportProgressDialog() {
        if (currentViewState?.dialogState is ShortcutListDialogState.ExportProgress) {
            updateDialogState(null)
        }
    }

    private fun getExportFormat() =
        if (settings.useLegacyExportFormat) ExportFormat.LEGACY_JSON else ExportFormat.ZIP

    fun onShortcutEdited() {
        logInfo("Shortcut editing completed")
        emitEvent(ShortcutListEvent.ShortcutEdited)
    }

    fun onDeletionConfirmed() {
        updateDialogState(null)
        val shortcutId = activeShortcutId ?: return
        val shortcut = getShortcutById(shortcutId) ?: return
        launchWithProgressTracking {
            shortcutRepository.deleteShortcut(shortcutId)
            pendingExecutionsRepository.removePendingExecutionsForShortcut(shortcutId)
            cancelAlarms(shortcutId)
            widgetsRepository.deleteDeadWidgets()
            showSnackbar(StringResLocalizable(R.string.shortcut_deleted, shortcut.name))
            updateLauncherSettings()
            emitEvent(ShortcutListEvent.RemoveShortcutFromHomeScreen(shortcut.toShortcutPlaceholder()))
        }
    }

    fun onDialogDismissed() {
        currentJob = null
        updateDialogState(null)
    }

    private fun updateDialogState(dialogState: ShortcutListDialogState?) {
        updateViewState {
            copy(dialogState = dialogState)
        }
    }

    data class InitData(
        val categoryId: CategoryId,
        val selectionMode: SelectionMode,
    )
}
