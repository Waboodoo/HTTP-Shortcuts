package ch.rmy.android.http_shortcuts.activities.main

import android.app.Activity
import android.app.Application
import android.net.Uri
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.createIntent
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.main.models.CategoryItem
import ch.rmy.android.http_shortcuts.activities.main.usecases.ShouldShowChangeLogDialogUseCase
import ch.rmy.android.http_shortcuts.activities.main.usecases.ShouldShowNetworkRestrictionDialogUseCase
import ch.rmy.android.http_shortcuts.activities.main.usecases.ShouldShowRecoveryDialogUseCase
import ch.rmy.android.http_shortcuts.activities.main.usecases.UnlockAppUseCase
import ch.rmy.android.http_shortcuts.data.domains.app_config.AppConfigRepository
import ch.rmy.android.http_shortcuts.data.domains.app_lock.AppLockRepository
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryRepository
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.PendingExecutionsRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.dtos.ShortcutPlaceholder
import ch.rmy.android.http_shortcuts.data.enums.SelectionMode
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.toShortcutPlaceholder
import ch.rmy.android.http_shortcuts.navigation.NavigationArgStore
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import ch.rmy.android.http_shortcuts.scheduling.ExecutionScheduler
import ch.rmy.android.http_shortcuts.utils.ActivityCloser
import ch.rmy.android.http_shortcuts.utils.AppOverlayUtil
import ch.rmy.android.http_shortcuts.utils.IntentUtil
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutUpdater
import ch.rmy.android.http_shortcuts.utils.SecondaryLauncherManager
import ch.rmy.android.http_shortcuts.utils.Settings
import ch.rmy.android.http_shortcuts.utils.VersionUtil
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.widget.WidgetManager
import ch.rmy.curlcommand.CurlCommand
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mindrot.jbcrypt.BCrypt

@HiltViewModel
class MainViewModel
@Inject
constructor(
    application: Application,
    private val categoryRepository: CategoryRepository,
    private val shortcutRepository: ShortcutRepository,
    private val appConfigRepository: AppConfigRepository,
    private val appLockRepository: AppLockRepository,
    private val temporaryShortcutRepository: TemporaryShortcutRepository,
    private val shouldShowRecoveryDialog: ShouldShowRecoveryDialogUseCase,
    private val shouldShowChangeLogDialog: ShouldShowChangeLogDialogUseCase,
    private val shouldShowNetworkRestrictionDialog: ShouldShowNetworkRestrictionDialogUseCase,
    private val executionScheduler: ExecutionScheduler,
    private val launcherShortcutManager: LauncherShortcutManager,
    private val launcherShortcutUpdater: LauncherShortcutUpdater,
    private val secondaryLauncherManager: SecondaryLauncherManager,
    private val widgetManager: WidgetManager,
    private val pendingExecutionsRepository: PendingExecutionsRepository,
    private val appOverlayUtil: AppOverlayUtil,
    private val variableRepository: VariableRepository,
    private val variablePlaceholderProvider: VariablePlaceholderProvider,
    private val settings: Settings,
    private val versionUtil: VersionUtil,
    private val unlockApp: UnlockAppUseCase,
    private val navigationArgStore: NavigationArgStore,
) : BaseViewModel<MainViewModel.InitData, MainViewState>(application) {

    private lateinit var categories: List<Category>

    private val selectionMode
        get() = initData.selectionMode

    private var activeShortcutId: ShortcutId? = null
    private var settingsRequestHandled: Boolean = false

    override suspend fun initialize(data: InitData): MainViewState {
        val categoriesFlow = categoryRepository.observeCategories()
        this.categories = categoriesFlow.first()

        viewModelScope.launch(Dispatchers.Default) {
            if (settings.firstSeenVersionCode == null) {
                settings.firstSeenVersionCode = versionUtil.getVersionCode()
            }
        }

        viewModelScope.launch(Dispatchers.Default) {
            // Ensure that the VariablePlaceholderProvider is initialized
            variablePlaceholderProvider.applyVariables(variableRepository.getVariables())
        }

        viewModelScope.launch {
            categoriesFlow.drop(1).collect { categories ->
                this@MainViewModel.categories = categories
                updateViewState {
                    copy(
                        categoryItems = getCategoryTabItems(),
                        activeCategoryId = (categories.find { it.id == activeCategoryId && !it.hidden } ?: categories.first { !it.hidden }).id,
                    )
                }
            }
        }

        val appLockObservable = appLockRepository.observeLock()
        val appLock = appLockObservable.firstOrNull()

        observeToolbarTitle()
        viewModelScope.launch {
            appLockObservable.collect { appLock ->
                updateViewState {
                    copy(isLocked = appLock != null)
                }
            }
        }

        viewModelScope.launch {
            if (initData.cancelPendingExecutions) {
                pendingExecutionsRepository.removeAllPendingExecutions()
                showSnackbar(R.string.message_pending_executions_cancelled)
            } else {
                scheduleExecutions()
            }
            updateLauncherSettings(categories)
        }

        viewModelScope.launch {
            if (data.importUrl != null && appLock == null) {
                navigate(NavigationDestination.ImportExport.buildRequest(data.importUrl))
            } else {
                when (selectionMode) {
                    SelectionMode.NORMAL -> showNormalStartupDialogsIfNeeded()
                    SelectionMode.HOME_SCREEN_SHORTCUT_PLACEMENT -> Unit
                    SelectionMode.HOME_SCREEN_WIDGET_PLACEMENT -> {
                        if (initData.widgetId != null) {
                            setActivityResult(Activity.RESULT_CANCELED, WidgetManager.getIntent(initData.widgetId!!))
                        }
                    }
                    SelectionMode.PLUGIN -> showPluginStartupDialogsIfNeeded()
                }
            }
        }

        return MainViewState(
            selectionMode = selectionMode,
            categoryItems = getCategoryTabItems(),
            activeCategoryId = initData.initialCategoryId ?: categories.first { !it.hidden }.id,
            hasMultipleCategories = categories.size > 1,
            isLocked = appLock != null,
        )
    }

    private fun getCategoryTabItems() =
        categories
            .runIf(selectionMode == SelectionMode.NORMAL) {
                filterNot { it.hidden }
            }
            .map { category ->
                CategoryItem(
                    categoryId = category.id,
                    name = category.name,
                    layoutType = category.layoutType,
                    background = category.background,
                    scale = category.scale,
                )
            }

    private suspend fun scheduleExecutions() {
        executionScheduler.schedule()
    }

    private fun showNormalStartupDialogsIfNeeded() {
        runAction {
            delay(500.milliseconds)
            val recoveryInfo = shouldShowRecoveryDialog()
            if (recoveryInfo != null && !viewState.isLocked) {
                updateDialogState(
                    MainDialogState.RecoverShortcut(
                        recoveryInfo = recoveryInfo,
                    ),
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
        settings.isChangeLogPermanentlyHidden = hidden
    }

    fun onNetworkRestrictionsWarningHidden(hidden: Boolean) {
        settings.isNetworkRestrictionWarningPermanentlyHidden = hidden
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

    private suspend fun updateLauncherSettings(categories: List<Category>) {
        withContext(Dispatchers.Default) {
            launcherShortcutUpdater.updateAppShortcuts()
            secondaryLauncherManager.setSecondaryLauncherVisibility(shortcutRepository.hasSecondaryLauncherShortcuts())
        }
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
        navigate(NavigationDestination.Variables.buildRequest())
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
            appConfigRepository.setToolbarTitle(newTitle)
        }
        showSnackbar(R.string.message_title_changed)
    }

    fun onActiveCategoryChanged(categoryId: CategoryId) = runAction {
        updateViewState {
            copy(activeCategoryId = categoryId)
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
                            appLockRepository.removeLock()
                            showSnackbar(R.string.message_app_unlocked)
                        }
                    }
                },
            )
        }
    }

    fun onAppLocked() = runAction {
        showSnackbar(R.string.message_app_locked)
    }

    fun onUnlockDialogSubmitted(password: String) = runAction {
        withProgressTracking {
            val lock = appLockRepository.getLock()
            val passwordHash = lock?.passwordHash
            val isUnlocked = if (passwordHash != null && BCrypt.checkpw(password, passwordHash)) {
                consume {
                    appLockRepository.removeLock()
                }
            } else {
                passwordHash == null
            }
            if (isUnlocked) {
                updateDialogState(null)
                showSnackbar(R.string.message_app_unlocked)
            } else {
                updateDialogState(MainDialogState.Progress)
                delay(Random.nextInt(from = 1, until = 4).seconds)
                updateDialogState(MainDialogState.Unlock(tryAgain = true))
            }
        }
    }

    fun onShortcutCreated(shortcutId: ShortcutId) = runAction {
        logInfo("Shortcut created")
        val categories = categoryRepository.getCategories()
        this@MainViewModel.categories = categories
        updateLauncherSettings(categories)
        selectShortcut(shortcutId)
    }

    private suspend fun selectShortcut(shortcutId: ShortcutId) {
        when (selectionMode) {
            SelectionMode.HOME_SCREEN_SHORTCUT_PLACEMENT -> returnForHomeScreenShortcutPlacement(shortcutId)
            SelectionMode.HOME_SCREEN_WIDGET_PLACEMENT -> openWidgetSettings(shortcutId, initData.widgetId)
            SelectionMode.PLUGIN -> returnForPlugin(shortcutId)
            SelectionMode.NORMAL -> Unit
        }
    }

    private suspend fun openWidgetSettings(shortcutId: ShortcutId, widgetId: Int?) {
        val shortcut = getShortcutById(shortcutId) ?: return
        navigate(
            NavigationDestination.Widget.buildRequest(
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

    private suspend fun returnForHomeScreenWidgetPlacement(
        shortcutId: ShortcutId,
        showLabel: Boolean,
        showIcon: Boolean,
        labelColor: String?,
        iconScale: Float,
    ) {
        val widgetId = initData.widgetId ?: return
        widgetManager.createWidget(widgetId, shortcutId, showLabel, showIcon, labelColor, iconScale)
        widgetManager.updateWidgets(context, shortcutId)
        finish(
            intent = WidgetManager.getIntent(widgetId),
            okResultCode = true,
        )
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
        } catch (e: NoSuchElementException) {
            null
        }

    fun onWidgetSettingsSubmitted(
        shortcutId: ShortcutId,
        showLabel: Boolean,
        showIcon: Boolean,
        labelColor: String?,
        iconScale: Float,
    ) = runAction {
        logInfo("Widget settings submitted")
        returnForHomeScreenWidgetPlacement(shortcutId, showLabel, showIcon, labelColor, iconScale)
    }

    fun onShortcutEdited() = runAction {
        logInfo("Shortcut edited")
        val categories = categoryRepository.getCategories()
        this@MainViewModel.categories = categories
        updateLauncherSettings(categories)
    }

    fun onChangesDiscarded() = runAction {
        showSnackbar(R.string.message_changes_discarded)
    }

    fun onPlaceShortcutOnHomeScreen(shortcut: ShortcutPlaceholder) = runAction {
        placeShortcutOnHomeScreen(shortcut)
    }

    fun onRemoveShortcutFromHomeScreen(shortcut: ShortcutPlaceholder) = runAction {
        removeShortcutFromHomeScreen(shortcut)
        val categories = categoryRepository.getCategories()
        this@MainViewModel.categories = categories
        updateLauncherSettings(categories)
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
        launcherShortcutUpdater.updateAppShortcuts()
        emitEvent(MainEvent.Restart)
    }

    fun onWidgetSettingsCancelled() = runAction {
        finish()
    }

    fun onApplicationSettingsRequested() = runAction {
        if (appLockRepository.getLock() != null || settingsRequestHandled) {
            skipAction()
        }
        settingsRequestHandled = true
        navigate(NavigationDestination.Settings)
    }

    data class InitData(
        val selectionMode: SelectionMode,
        val initialCategoryId: CategoryId?,
        val widgetId: Int?,
        val importUrl: Uri?,
        val cancelPendingExecutions: Boolean,
    )
}
