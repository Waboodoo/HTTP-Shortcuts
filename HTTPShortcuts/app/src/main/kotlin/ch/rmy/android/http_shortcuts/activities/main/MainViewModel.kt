package ch.rmy.android.http_shortcuts.activities.main

import android.app.Activity
import android.app.Application
import android.net.Uri
import android.os.Build
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.createIntent
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.ViewModelScope
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.main.models.CategoryItem
import ch.rmy.android.http_shortcuts.activities.main.usecases.ShouldShowChangeLogDialogUseCase
import ch.rmy.android.http_shortcuts.activities.main.usecases.ShouldShowNetworkRestrictionDialogUseCase
import ch.rmy.android.http_shortcuts.activities.main.usecases.ShouldShowRecoveryDialogUseCase
import ch.rmy.android.http_shortcuts.activities.main.usecases.UnlockAppUseCase
import ch.rmy.android.http_shortcuts.applock.AppLockController
import ch.rmy.android.http_shortcuts.data.domains.app_config.AppConfigRepository
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryRepository
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.PendingExecutionsRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.GlobalVariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.GlobalVariableRepository
import ch.rmy.android.http_shortcuts.data.domains.widgets.ShortcutWidgetsRepository
import ch.rmy.android.http_shortcuts.data.dtos.ShortcutPlaceholder
import ch.rmy.android.http_shortcuts.data.enums.CategoryAlignment
import ch.rmy.android.http_shortcuts.data.enums.SelectionMode
import ch.rmy.android.http_shortcuts.data.enums.WidgetBackgroundType
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.settings.DeviceLocalPreferences
import ch.rmy.android.http_shortcuts.data.settings.UserPreferences
import ch.rmy.android.http_shortcuts.extensions.toShortcutPlaceholder
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.navigation.NavigationArgStore
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import ch.rmy.android.http_shortcuts.scheduling.ExecutionScheduler
import ch.rmy.android.http_shortcuts.shell_apk.ShellApkBuilder
import ch.rmy.android.http_shortcuts.sync.ObserveSyncReplaceUseCase
import ch.rmy.android.http_shortcuts.utils.ActivityCloser
import ch.rmy.android.http_shortcuts.utils.AppOverlayUtil
import ch.rmy.android.http_shortcuts.utils.IntentUtil
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutUpdater
import ch.rmy.android.http_shortcuts.utils.ShortcutUpdateWorker
import ch.rmy.android.http_shortcuts.utils.VersionUtil
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.widget.ShortcutWidgetManager
import ch.rmy.android.http_shortcuts.widget.VariableWidgetManager
import ch.rmy.android.http_shortcuts.widget.WidgetsUtil
import ch.rmy.curlcommand.CurlCommand
import dagger.hilt.android.lifecycle.HiltViewModel
import java.net.URLEncoder
import javax.inject.Inject
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class MainViewModel
@Inject
constructor(
    application: Application,
    private val categoryRepository: CategoryRepository,
    private val shortcutRepository: ShortcutRepository,
    private val appConfigRepository: AppConfigRepository,
    private val appLockController: AppLockController,
    private val temporaryShortcutRepository: TemporaryShortcutRepository,
    private val shouldShowRecoveryDialog: ShouldShowRecoveryDialogUseCase,
    private val shouldShowChangeLogDialog: ShouldShowChangeLogDialogUseCase,
    private val shouldShowNetworkRestrictionDialog: ShouldShowNetworkRestrictionDialogUseCase,
    private val executionScheduler: ExecutionScheduler,
    private val launcherShortcutManager: LauncherShortcutManager,
    private val launcherShortcutUpdater: LauncherShortcutUpdater,
    private val shortcutWidgetManager: ShortcutWidgetManager,
    private val shortcutWidgetsRepository: ShortcutWidgetsRepository,
    private val variableWidgetManager: VariableWidgetManager,
    private val pendingExecutionsRepository: PendingExecutionsRepository,
    private val appOverlayUtil: AppOverlayUtil,
    private val globalVariableRepository: GlobalVariableRepository,
    private val variablePlaceholderProvider: VariablePlaceholderProvider,
    private val deviceLocalPreferences: DeviceLocalPreferences,
    private val userPreferences: UserPreferences,
    private val versionUtil: VersionUtil,
    private val unlockApp: UnlockAppUseCase,
    private val navigationArgStore: NavigationArgStore,
    private val observeSyncReplace: ObserveSyncReplaceUseCase,
    private val shortcutUpdateWorkerStarter: ShortcutUpdateWorker.Starter,
    private val shellApkBuilder: ShellApkBuilder,
) : BaseViewModel<MainViewModel.InitData, MainViewState>(application) {

    private lateinit var categories: List<Category>

    private val selectionMode
        get() = initData.selectionMode

    private var activeShortcutId: ShortcutId? = null
    private var settingsRequestHandled: Boolean = false
    private var switchedAwayFromInitialCategory = false
    private var shortcutForApkExport: ShortcutPlaceholder? = null

    override suspend fun initialize(data: InitData): MainViewState {
        logInfo("Init with mode=${data.selectionMode}")
        val categoriesFlow = categoryRepository.observeCategories()
        this.categories = categoriesFlow.first()
        if (categories.isEmpty()) {
            categoryRepository.createInitialCategory(context.getString(R.string.shortcuts))
            this.categories = categoriesFlow.filter { it.isNotEmpty() }.first()
        }

        viewModelScope.launch(Dispatchers.Default) {
            if (deviceLocalPreferences.firstSeenVersionCode == null) {
                deviceLocalPreferences.firstSeenVersionCode = versionUtil.getVersionCode()
            }
        }

        viewModelScope.launch(Dispatchers.Default) {
            // Ensure that the VariablePlaceholderProvider is initialized
            variablePlaceholderProvider.applyVariables(globalVariableRepository.getGlobalVariables())
        }

        viewModelScope.launch(Dispatchers.Default) {
            categoriesFlow.drop(1).collect { categories ->
                this@MainViewModel.categories = categories
                updateViewState {
                    copy(
                        categoryItems = getCategoryTabItems(categories),
                        activeCategoryId = (categories.find { it.id == activeCategoryId && !it.hidden } ?: categories.first { !it.hidden }).id,
                    )
                }
            }
        }

        val isAppLockedFlow = appLockController.observeLocked()
        val isAppLocked = isAppLockedFlow.first()
        observeToolbarTitle()
        viewModelScope.launch(Dispatchers.Default) {
            isAppLockedFlow
                .distinctUntilChanged()
                .drop(1)
                .collect { isLocked ->
                    updateViewState {
                        copy(
                            isLocked = isLocked,
                            dialogState = if (isLocked) null else dialogState,
                        )
                    }
                    if (isLocked) {
                        showSnackbar(R.string.message_app_locked)
                    } else {
                        showSnackbar(R.string.message_app_unlocked)
                    }
                }
        }

        val isInSyncReplaceMode = monitorFlow(observeSyncReplace(), Dispatchers.Default) { isInSyncReplaceMode ->
            updateViewState {
                copy(
                    isInSyncReplaceMode = isInSyncReplaceMode,
                )
            }
        }

        val appHasLockFlow = appLockController.observeLock()
            .map { it != null }
        val appHasLock = monitorFlow(appHasLockFlow, Dispatchers.Default) { hasLock ->
            updateViewState {
                copy(hasLock = hasLock)
            }
        }

        viewModelScope.launch(Dispatchers.Default) {
            if (initData.cancelPendingExecutions) {
                pendingExecutionsRepository.removeAllPendingExecutions()
                showSnackbar(R.string.message_pending_executions_cancelled)
            } else {
                scheduleExecutions()
            }
            shortcutUpdateWorkerStarter.invoke()
        }

        val widgetShortcutForEditing = initData.widgetId
            ?.takeIf { selectionMode == SelectionMode.SHORTCUT_WIDGET_PLACEMENT }
            ?.let { widgetId ->
                shortcutWidgetsRepository.getShortcutWidgetById(widgetId)
            }
            ?.let { widget ->
                try {
                    shortcutRepository.getShortcutById(widget.shortcutId)
                } catch (_: NoSuchElementException) {
                    null
                }
            }

        viewModelScope.launch {
            if (data.importUrl != null && !isAppLocked) {
                navigate(NavigationDestination.ImportExport.buildRequest(data.importUrl))
            } else {
                when (selectionMode) {
                    SelectionMode.NORMAL -> showNormalStartupDialogsIfNeeded(isAppLockedFlow)
                    SelectionMode.HOME_SCREEN_SHORTCUT_PLACEMENT -> Unit
                    SelectionMode.SHORTCUT_WIDGET_PLACEMENT -> {
                        initData.widgetId?.let { widgetId ->
                            setActivityResult(Activity.RESULT_CANCELED, WidgetsUtil.getIntent(widgetId))
                        }
                    }
                    SelectionMode.VARIABLE_WIDGET_PLACEMENT -> {
                        initData.widgetId?.let { widgetId ->
                            navigate(NavigationDestination.VariableWidget.buildRequest(widgetId))
                            setActivityResult(Activity.RESULT_CANCELED, WidgetsUtil.getIntent(widgetId))
                        }
                    }
                    SelectionMode.PLUGIN -> showPluginStartupDialogsIfNeeded()
                }
            }
        }

        return MainViewState(
            selectionMode = selectionMode,
            categoryItems = getCategoryTabItems(categories),
            activeCategoryId = initData.initialCategoryId
                ?: (widgetShortcutForEditing?.categoryId ?: deviceLocalPreferences.lastActiveCategoryId)
                    ?.takeIf { categoryId -> categories.find { it.id == categoryId }?.hidden == false }
                ?: categories.first { !it.hidden }.id,
            hasMultipleCategories = categories.size > 1,
            isLocked = isAppLocked,
            hasLock = appHasLock,
            isInSyncReplaceMode = isInSyncReplaceMode,
            highlightedShortcutId = widgetShortcutForEditing?.id,
        )
    }

    private fun getCategoryTabItems(categories: List<Category>) =
        categories
            .runIf(selectionMode == SelectionMode.NORMAL) {
                filterNot { it.hidden }
            }
            .map { category ->
                CategoryItem(
                    categoryId = category.id,
                    name = category.name,
                    layoutType = category.layoutType,
                    alignment = category.alignment ?: CategoryAlignment.TOP,
                    background = category.background,
                    scale = category.scale,
                    hiddenLabels = category.hiddenLabels,
                )
            }

    private suspend fun scheduleExecutions() {
        executionScheduler.schedule()
    }

    private fun showNormalStartupDialogsIfNeeded(isAppLockedFlow: Flow<Boolean>) {
        runAction {
            isAppLockedFlow.first { !it }
            delay(500.milliseconds)
            val recoveryInfo = shouldShowRecoveryDialog()
            if (recoveryInfo != null) {
                updateDialogState(
                    MainDialogState.RecoverShortcut(
                        recoveryInfo = recoveryInfo,
                    ),
                )
            } else if (deviceLocalPreferences.syncTooManyErrors) {
                deviceLocalPreferences.syncTooManyErrors = false
                updateDialogState(
                    MainDialogState.TooManySyncErrors,
                )
            } else if (shouldShowChangeLogDialog()) {
                updateDialogState(
                    MainDialogState.ChangeLog,
                )
            } else {
                showNetworkRestrictionWarningDialogIfNeeded()
            }
        }
    }

    fun onChangelogPermanentlyHiddenChanged(hidden: Boolean) {
        deviceLocalPreferences.isChangeLogPermanentlyHidden = hidden
    }

    fun onNetworkRestrictionsWarningHidden(hidden: Boolean) {
        deviceLocalPreferences.isNetworkRestrictionWarningPermanentlyHidden = hidden
    }

    fun onRecoveryConfirmed() = runAction {
        logInfo("Shortcut recovery confirmed")
        val recoveryInfo = (viewState.dialogState as? MainDialogState.RecoverShortcut)
            ?.recoveryInfo
            ?: skipAction()
        updateDialogState(null)
        navigate(
            NavigationDestination.ShortcutEditor.buildRequest(
                shortcutId = recoveryInfo.shortcutId,
                categoryId = recoveryInfo.categoryId,
                recoveryMode = true,
            ),
        )
    }

    fun onRecoveryDiscarded() = runAction {
        logInfo("Shortcut recovery discarded")
        updateDialogState(null)
        withProgressTracking {
            temporaryShortcutRepository.deleteTemporaryShortcut()
        }
    }

    private suspend fun showNetworkRestrictionWarningDialogIfNeeded() {
        if (shouldShowNetworkRestrictionDialog()) {
            updateDialogState(
                MainDialogState.NetworkRestrictionsWarning,
            )
        }
    }

    private suspend fun showPluginStartupDialogsIfNeeded() {
        if (!appOverlayUtil.canDrawOverlays()) {
            updateDialogState(
                MainDialogState.AppOverlayInfo,
            )
        }
    }

    fun onAppOverlayConfigureButtonClicked() = runAction {
        updateDialogState(null)
        sendIntent(appOverlayUtil.getSettingsIntent())
    }

    private suspend fun placeShortcutOnHomeScreen(shortcutPlaceholder: ShortcutPlaceholder) {
        if (launcherShortcutManager.supportsPinning()) {
            withContext(Dispatchers.Default) {
                launcherShortcutUpdater.pinShortcut(shortcutPlaceholder.id)
            }
        } else {
            sendBroadcast(IntentUtil.getLegacyShortcutPlacementIntent(context, shortcutPlaceholder, install = true))
            showSnackbar(StringResLocalizable(R.string.shortcut_placed, shortcutPlaceholder.name))
        }
    }

    private suspend fun removeShortcutFromHomeScreen(shortcut: ShortcutPlaceholder) {
        sendBroadcast(IntentUtil.getLegacyShortcutPlacementIntent(context, shortcut, install = false))
    }

    private fun observeToolbarTitle() {
        viewModelScope.launch {
            appConfigRepository.observeToolbarTitle().collect { toolbarTitle ->
                updateViewState {
                    copy(toolbarTitle = toolbarTitle)
                }
            }
        }
    }

    fun onSettingsButtonClicked() = runAction {
        logInfo("Settings button clicked")
        navigate(NavigationDestination.Settings)
    }

    fun onImportExportButtonClicked() = runAction {
        logInfo("Import/export button clicked")
        navigate(NavigationDestination.ImportExport.buildRequest())
    }

    fun onTroubleShootingButtonClicked() = runAction {
        logInfo("Trouble Shooting button clicked")
        navigate(NavigationDestination.TroubleShooting)
    }

    fun onAboutButtonClicked() = runAction {
        logInfo("About button clicked")
        navigate(NavigationDestination.About)
    }

    fun onCategoriesButtonClicked() = runAction {
        logInfo("Categories button clicked")
        openCategoriesEditor()
    }

    private fun openCategoriesEditor() = runAction {
        navigate(NavigationDestination.Categories)
    }

    fun onVariablesButtonClicked() = runAction {
        logInfo("Variables button clicked")
        navigate(NavigationDestination.GlobalVariables.buildRequest())
    }

    fun onWorkingDirectoriesClicked() = runAction {
        logInfo("Working directories button clicked")
        navigate(NavigationDestination.WorkingDirectories.buildRequest())
    }

    fun onToolbarTitleClicked() = runAction {
        logInfo("Toolbar title clicked")
        if (selectionMode == SelectionMode.NORMAL && !viewState.isLocked) {
            showToolbarTitleChangeDialog(viewState.toolbarTitle)
        }
    }

    private suspend fun showToolbarTitleChangeDialog(oldTitle: String) {
        updateDialogState(
            MainDialogState.ChangeTitle(oldTitle),
        )
    }

    fun onCreateShortcutButtonClicked() = runAction {
        logInfo("Shortcut creation FAB clicked")
        navigate(
            NavigationDestination.TypePicker.buildRequest(viewState.activeCategoryId),
        )
    }

    fun onToolbarTitleChangeSubmitted(newTitle: String) = runAction {
        updateDialogState(null)
        withProgressTracking {
            if (newTitle == appConfigRepository.getToolbarTitle()) {
                skipAction()
            }
            appConfigRepository.setToolbarTitle(newTitle)
        }
        showSnackbar(R.string.message_title_changed)
    }

    fun onActiveCategoryChanged(categoryId: CategoryId) = runAction {
        updateViewState {
            copy(activeCategoryId = categoryId)
        }
        if (categoryId != initData.initialCategoryId || switchedAwayFromInitialCategory) {
            switchedAwayFromInitialCategory = true
            if (userPreferences.isRememberActiveCategory) {
                deviceLocalPreferences.lastActiveCategoryId = categoryId
            }
        }
    }

    fun onUnlockButtonClicked() = runAction {
        logInfo("Unlock button clicked")
        withProgressTracking {
            unlockApp(
                showPasswordDialog = {
                    runAction {
                        updateDialogState(
                            MainDialogState.Unlock(),
                        )
                    }
                },
                onSuccess = {
                    runAction {
                        withProgressTracking {
                            appLockController.unlock()
                        }
                    }
                },
            )
        }
    }

    fun onLockButtonClicked() = runAction {
        logInfo("Lock button clicked")
        appLockController.lock()
    }

    fun onUnlockDialogSubmitted(password: String) = runAction {
        withProgressTracking {
            if (appLockController.isPasswordCorrect(password)) {
                updateDialogState(null)
                appLockController.unlock()
            } else {
                updateDialogState(MainDialogState.Progress)
                delay(Random.nextInt(from = 1000, until = 5000).milliseconds)
                updateDialogState(MainDialogState.Unlock(tryAgain = true))
            }
        }
    }

    fun onShortcutCreated(shortcutId: ShortcutId) = runAction {
        logInfo("Shortcut created")
        selectShortcut(shortcutId)
    }

    private suspend fun selectShortcut(shortcutId: ShortcutId) {
        when (selectionMode) {
            SelectionMode.HOME_SCREEN_SHORTCUT_PLACEMENT -> returnForHomeScreenShortcutPlacement(shortcutId)
            SelectionMode.SHORTCUT_WIDGET_PLACEMENT -> {
                updateViewState {
                    copy(highlightedShortcutId = shortcutId)
                }
                openShortcutWidgetSettings(shortcutId, initData.widgetId)
            }
            SelectionMode.PLUGIN -> returnForPlugin(shortcutId)
            SelectionMode.VARIABLE_WIDGET_PLACEMENT,
            SelectionMode.NORMAL,
                -> Unit
        }
    }

    private suspend fun openShortcutWidgetSettings(shortcutId: ShortcutId, widgetId: Int?) {
        val shortcut = getShortcutById(shortcutId) ?: return
        navigate(
            NavigationDestination.ShortcutWidget.buildRequest(
                shortcutId = shortcut.id,
                shortcutName = shortcut.name,
                shortcutIcon = shortcut.icon,
                widgetId = widgetId,
            ),
        )
    }

    private suspend fun returnForHomeScreenShortcutPlacement(shortcutId: ShortcutId) {
        if (launcherShortcutManager.supportsPinning()) {
            activeShortcutId = shortcutId
            updateDialogState(
                MainDialogState.ShortcutPlacement,
            )
        } else {
            placeOnHomeScreenWithLegacyAndFinish(shortcutId)
        }
    }

    fun onShortcutPlacementConfirmed(useLegacyMethod: Boolean) = runAction {
        updateDialogState(null)
        val shortcutId = activeShortcutId ?: skipAction()
        if (useLegacyMethod) {
            placeOnHomeScreenWithLegacyAndFinish(shortcutId)
        } else {
            placeOnHomeScreenAndFinish(shortcutId)
        }
    }

    private suspend fun placeOnHomeScreenAndFinish(shortcutId: ShortcutId) {
        finish(
            intent = launcherShortcutUpdater.createShortcutPinIntent(shortcutId),
            okResultCode = true,
        )
    }

    private suspend fun placeOnHomeScreenWithLegacyAndFinish(shortcutId: ShortcutId) {
        val shortcut = getShortcutById(shortcutId) ?: return
        finish(
            intent = IntentUtil.getLegacyShortcutPlacementIntent(context, shortcut.toShortcutPlaceholder(), install = true),
            okResultCode = true,
        )
    }

    private suspend fun returnForPlugin(shortcutId: ShortcutId) {
        val shortcut = getShortcutById(shortcutId) ?: return
        finish(
            intent = createIntent {
                putExtra(MainActivity.EXTRA_SELECTION_ID, shortcut.id)
                putExtra(MainActivity.EXTRA_SELECTION_NAME, shortcut.name)
            },
            okResultCode = true,
        )
    }

    fun onCurlCommandSubmitted(curlCommand: CurlCommand) = runAction {
        logInfo("curl command submitted")
        val curlCommandId = navigationArgStore.storeArg(curlCommand)
        navigate(
            NavigationDestination.ShortcutEditor.buildRequest(
                categoryId = viewState.activeCategoryId,
                curlCommandId = curlCommandId,
            ),
        )
    }

    private suspend fun getShortcutById(shortcutId: ShortcutId): Shortcut? =
        try {
            shortcutRepository.getShortcutById(shortcutId)
        } catch (_: NoSuchElementException) {
            null
        }

    fun onShortcutWidgetSettingsSubmitted(
        shortcutId: ShortcutId,
        showLabel: Boolean,
        showIcon: Boolean,
        labelColor: String?,
        iconScale: Float,
    ) = runAction {
        logInfo("Shortcut widget settings submitted")
        val widgetId = initData.widgetId ?: skipAction()
        shortcutWidgetsRepository.createOrUpdateShortcutWidget(widgetId, shortcutId, showLabel, showIcon, labelColor, iconScale)
        shortcutWidgetManager.updateWidgets(shortcutId)
        finish(
            intent = WidgetsUtil.getIntent(widgetId),
            okResultCode = true,
        )
    }

    fun onVariableWidgetSettingsSubmitted(
        variableId: GlobalVariableId,
        fontSize: Int,
        title: String,
        background: WidgetBackgroundType?,
        shortcutId: ShortcutId?,
    ) = runAction {
        logInfo("Variable widget settings submitted")
        val widgetId = initData.widgetId ?: skipAction()
        variableWidgetManager.createOrUpdateWidget(
            widgetId = widgetId,
            globalVariableId = variableId,
            fontSize = fontSize,
            title = title,
            background = background,
            shortcutId = shortcutId,
        )
        variableWidgetManager.updateWidgets(variableId)
        finish(
            intent = WidgetsUtil.getIntent(widgetId),
            okResultCode = true,
        )
    }

    fun onShortcutEdited() = runAction {
        logInfo("Shortcut edited")
    }

    fun onChangesDiscarded() = runAction {
        showSnackbar(R.string.message_changes_discarded)
    }

    fun onPlaceShortcutOnHomeScreen(shortcut: ShortcutPlaceholder) = runAction {
        placeShortcutOnHomeScreen(shortcut)
    }

    fun onInstallShortcutAsApp(shortcut: ShortcutPlaceholder) = runAction {
        withProgressTracking {
            if (deviceLocalPreferences.isAwareOfShellApks) {
                openPickerForApk(shortcut)
            } else {
                deviceLocalPreferences.isAwareOfShellApks = true
                updateDialogState(
                    MainDialogState.ShellApkInfo(shortcut),
                )
            }
        }
    }

    private suspend fun openPickerForApk(shortcut: ShortcutPlaceholder) {
        shortcutForApkExport = shortcut
        emitEvent(MainEvent.PickFileForApk(fileName = generateApkName(shortcut.name)))
    }

    private fun generateApkName(shortcutName: String): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            URLEncoder.encode(shortcutName, Charsets.UTF_8)
        } else {
            URLEncoder.encode(shortcutName)
        }
            .trim()
            .plus(".apk")

    fun onShellApkInfoConfirmed() = runAction {
        val shortcut = (getCurrentViewState().dialogState as? MainDialogState.ShellApkInfo)?.shortcut ?: skipAction()
        updateDialogState(null)
        openPickerForApk(shortcut)
    }

    fun onFilePickedForApk(fileUri: Uri) = runAction {
        logInfo("Building shell APK")
        try {
            updateDialogState(MainDialogState.Progress)
            val shortcut = shortcutForApkExport ?: skipAction()
            val apkFile = shellApkBuilder.build(
                shortcutId = shortcut.id,
                appName = shortcut.name,
                icon = shortcut.icon,
            )
            withContext(Dispatchers.IO) {
                apkFile.inputStream().use { inputStream ->
                    context.contentResolver.openOutputStream(fileUri)?.use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }
            showSnackbar(R.string.message_shell_apk_saved)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleUnexpectedError(e)
        } finally {
            updateDialogState(null)
        }
    }

    fun onRemoveShortcutFromHomeScreen(shortcut: ShortcutPlaceholder) = runAction {
        removeShortcutFromHomeScreen(shortcut)
        shortcutUpdateWorkerStarter.invoke()
    }

    fun onSelectShortcut(shortcutId: ShortcutId) = runAction {
        logInfo("Shortcut selected")
        selectShortcut(shortcutId)
    }

    fun onDialogDismissed() = runAction {
        if (viewState.dialogState is MainDialogState.ChangeLog && shouldShowNetworkRestrictionDialog()) {
            updateDialogState(
                MainDialogState.NetworkRestrictionsWarning,
            )
        } else {
            updateDialogState(null)
        }
    }

    private suspend fun updateDialogState(dialogState: MainDialogState?) {
        updateViewState {
            copy(dialogState = dialogState)
        }
    }

    fun onBackButtonPressed() = runAction {
        finish()
        ActivityCloser.onMainActivityClosed()
    }

    fun onShortcutsOrCategoriesChanged() = runAction {
        shortcutUpdateWorkerStarter.invoke()
        emitEvent(MainEvent.Restart(viewState.activeCategoryId))
    }

    fun onShortcutWidgetSettingsCancelled() = runAction {
        finish()
    }

    fun onVariableWidgetSettingsCancelled() = runAction {
        finish()
    }

    fun onApplicationSettingsRequested() = runAction {
        if (viewState.isLocked || settingsRequestHandled) {
            skipAction()
        }
        settingsRequestHandled = true
        navigate(NavigationDestination.Settings)
    }

    fun onLongPress() = runAction {
        if (viewState.isLocked) {
            skipAction()
        }
        val category = getActiveCategory() ?: skipAction()
        updateDialogState(
            MainDialogState.CategoryMenu(
                title = category.name,
                placeOnHomeScreenOptionVisible = launcherShortcutManager.supportsPinning(),
            ),
        )
    }

    private fun ViewModelScope<MainViewState>.getActiveCategory(): Category? =
        categories.find { it.id == viewState.activeCategoryId }

    fun onEditCategoryClicked() = runAction {
        updateDialogState(null)
        navigate(NavigationDestination.CategoryEditor.buildRequest(viewState.activeCategoryId))
    }

    fun onManageSectionsClicked() = runAction {
        updateDialogState(null)
        navigate(NavigationDestination.CategorySectionsEditor.buildRequest(viewState.activeCategoryId))
    }

    fun onPlaceCategoryOnHomeScreenClicked() = runAction {
        updateDialogState(null)
        val category = getActiveCategory() ?: skipAction()
        updateDialogState(
            MainDialogState.CategoryIconPicker(
                currentIcon = (category.icon as? ShortcutIcon.BuiltInIcon)
                    ?: ShortcutIcon.BuiltInIcon.fromDrawableResource(context, R.drawable.flat_grey_folder),
                suggestionBase = category.name,
            ),
        )
    }

    fun onCategoryIconSelected(icon: ShortcutIcon) = runAction {
        updateDialogState(null)
        val category = getActiveCategory() ?: skipAction()
        withProgressTracking {
            categoryRepository.setCategoryIcon(category.id, icon)
            withContext(Dispatchers.Default) {
                launcherShortcutManager.updatePinnedCategoryShortcut(category.id, category.name, icon)
                launcherShortcutManager.pinCategory(category.id, category.name, icon)
            }
        }
    }

    fun onMaterialDesignCategoryIconOptionSelected() = runAction {
        updateDialogState(null)
        navigate(NavigationDestination.IconPicker.buildRequest(materialDesignIcon = true))
    }

    fun onCustomCategoryIconOptionSelected() = runAction {
        updateDialogState(null)
        navigate(NavigationDestination.IconPicker.buildRequest())
    }

    data class InitData(
        val selectionMode: SelectionMode,
        val initialCategoryId: CategoryId?,
        val widgetId: Int?,
        val importUrl: Uri?,
        val cancelPendingExecutions: Boolean,
    )
}
