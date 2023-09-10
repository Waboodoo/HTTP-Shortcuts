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
import ch.rmy.android.http_shortcuts.activities.main.usecases.LauncherShortcutMapperUseCase
import ch.rmy.android.http_shortcuts.activities.main.usecases.SecondaryLauncherMapperUseCase
import ch.rmy.android.http_shortcuts.activities.main.usecases.ShouldShowChangeLogDialogUseCase
import ch.rmy.android.http_shortcuts.activities.main.usecases.ShouldShowNetworkRestrictionDialogUseCase
import ch.rmy.android.http_shortcuts.activities.main.usecases.ShouldShowRecoveryDialogUseCase
import ch.rmy.android.http_shortcuts.activities.main.usecases.UnlockAppUseCase
import ch.rmy.android.http_shortcuts.data.domains.app.AppRepository
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryRepository
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.PendingExecutionsRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.dtos.ShortcutPlaceholder
import ch.rmy.android.http_shortcuts.data.enums.SelectionMode
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.toShortcutPlaceholder
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import ch.rmy.android.http_shortcuts.scheduling.ExecutionScheduler
import ch.rmy.android.http_shortcuts.utils.ActivityCloser
import ch.rmy.android.http_shortcuts.utils.AppOverlayUtil
import ch.rmy.android.http_shortcuts.utils.ExternalURLs
import ch.rmy.android.http_shortcuts.utils.IntentUtil
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import ch.rmy.android.http_shortcuts.utils.SecondaryLauncherManager
import ch.rmy.android.http_shortcuts.utils.Settings
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.widget.WidgetManager
import ch.rmy.curlcommand.CurlCommand
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt
import javax.inject.Inject
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class MainViewModel
@Inject
constructor(
    application: Application,
    private val categoryRepository: CategoryRepository,
    private val appRepository: AppRepository,
    private val launcherShortcutMapper: LauncherShortcutMapperUseCase,
    private val secondaryLauncherMapper: SecondaryLauncherMapperUseCase,
    private val temporaryShortcutRepository: TemporaryShortcutRepository,
    private val shouldShowRecoveryDialog: ShouldShowRecoveryDialogUseCase,
    private val shouldShowChangeLogDialog: ShouldShowChangeLogDialogUseCase,
    private val shouldShowNetworkRestrictionDialog: ShouldShowNetworkRestrictionDialogUseCase,
    private val executionScheduler: ExecutionScheduler,
    private val launcherShortcutManager: LauncherShortcutManager,
    private val secondaryLauncherManager: SecondaryLauncherManager,
    private val widgetManager: WidgetManager,
    private val pendingExecutionsRepository: PendingExecutionsRepository,
    private val appOverlayUtil: AppOverlayUtil,
    private val variableRepository: VariableRepository,
    private val variablePlaceholderProvider: VariablePlaceholderProvider,
    private val settings: Settings,
    private val unlockApp: UnlockAppUseCase,
) : BaseViewModel<MainViewModel.InitData, MainViewState>(application) {

    private lateinit var categories: List<Category>

    private val selectionMode
        get() = initData.selectionMode

    private var activeShortcutId: ShortcutId? = null

    override suspend fun initialize(data: InitData): MainViewState {
        this.categories = categoryRepository.getCategories()

        viewModelScope.launch(Dispatchers.Default) {
            // Ensure that the VariablePlaceholderProvider is initialized
            variablePlaceholderProvider.applyVariables(variableRepository.getVariables())
        }

        observeToolbarTitle()
        observeAppLock()

        viewModelScope.launch {
            if (initData.cancelPendingExecutions) {
                pendingExecutionsRepository.removeAllPendingExecutions()
            } else {
                scheduleExecutions()
            }
            updateLauncherSettings(categories)
        }

        viewModelScope.launch {
            if (data.importUrl != null) {
                navigate(NavigationDestination.ImportExport.buildRequest(data.importUrl))
            } else {
                when (selectionMode) {
                    SelectionMode.NORMAL -> showNormalStartupDialogsIfNeeded()
                    SelectionMode.HOME_SCREEN_SHORTCUT_PLACEMENT -> showToast(R.string.instructions_select_shortcut_for_home_screen, long = true)
                    SelectionMode.HOME_SCREEN_WIDGET_PLACEMENT -> {
                        if (initData.widgetId != null) {
                            setActivityResult(Activity.RESULT_CANCELED, WidgetManager.getIntent(initData.widgetId!!))
                        }
                        showToast(R.string.instructions_select_shortcut_for_home_screen, long = true)
                    }
                    SelectionMode.PLUGIN -> showPluginStartupDialogsIfNeeded()
                }
            }
        }

        return MainViewState(
            selectionMode = selectionMode,
            categoryItems = getCategoryTabItems(),
            activeCategoryId = initData.initialCategoryId ?: categories.first { !it.hidden }.id,
            isLocked = false,
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
                    layoutType = category.categoryLayoutType,
                    background = category.categoryBackgroundType,
                )
            }

    private suspend fun scheduleExecutions() {
        executionScheduler.schedule()
    }

    private fun showNormalStartupDialogsIfNeeded() {
        viewModelScope.launch {
            delay(500.milliseconds)
            val recoveryInfo = shouldShowRecoveryDialog()
            if (recoveryInfo != null) {
                updateDialogState(
                    MainDialogState.RecoverShortcut(
                        recoveryInfo = recoveryInfo,
                    )
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
            )
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
        appOverlayUtil.getSettingsIntent()
            ?.let { sendIntent(it) }
    }

    private fun updateLauncherSettings(categories: List<Category>) {
        launcherShortcutManager.updateAppShortcuts(launcherShortcutMapper(categories))
        secondaryLauncherManager.setSecondaryLauncherVisibility(secondaryLauncherMapper(categories))
    }

    private suspend fun placeShortcutOnHomeScreen(shortcut: ShortcutPlaceholder) {
        if (launcherShortcutManager.supportsPinning()) {
            launcherShortcutManager.pinShortcut(shortcut)
        } else {
            sendBroadcast(IntentUtil.getLegacyShortcutPlacementIntent(context, shortcut, install = true))
            showSnackbar(StringResLocalizable(R.string.shortcut_placed, shortcut.name))
        }
    }

    private suspend fun removeShortcutFromHomeScreen(shortcut: ShortcutPlaceholder) {
        sendBroadcast(IntentUtil.getLegacyShortcutPlacementIntent(context, shortcut, install = false))
    }

    private fun observeToolbarTitle() {
        viewModelScope.launch {
            appRepository.getObservableToolbarTitle().collect { toolbarTitle ->
                updateViewState {
                    copy(toolbarTitle = toolbarTitle)
                }
            }
        }
    }

    private fun observeAppLock() {
        viewModelScope.launch {
            appRepository.getObservableLock().collect { appLock ->
                updateViewState {
                    copy(isLocked = appLock != null)
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
        navigate(NavigationDestination.Variables)
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

    fun onCreationDialogOptionSelected(executionType: ShortcutExecutionType) = runAction {
        logInfo("Preparing to open editor for creating shortcut of type $executionType")
        updateDialogState(null)
        navigate(
            NavigationDestination.ShortcutEditor.buildRequest(
                categoryId = viewState.activeCategoryId,
                executionType = executionType,
            )
        )
    }

    fun onCreationDialogHelpButtonClicked() = runAction {
        logInfo("Shortcut creation help button clicked")
        updateDialogState(null)
        openURL(ExternalURLs.SHORTCUTS_DOCUMENTATION)
    }

    fun onCreateShortcutButtonClicked() = runAction {
        logInfo("Shortcut creation FAB clicked")
        updateDialogState(
            MainDialogState.ShortcutCreation,
        )
    }

    fun onToolbarTitleChangeSubmitted(newTitle: String) = runAction {
        updateDialogState(null)
        withProgressTracking {
            appRepository.setToolbarTitle(newTitle)
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
                            appRepository.removeLock()
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
            val lock = appRepository.getLock()
            val passwordHash = lock?.passwordHash
            val isUnlocked = if (passwordHash != null && BCrypt.checkpw(password, passwordHash)) consume {
                appRepository.removeLock()
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

    fun onCurlImportOptionSelected() = runAction {
        logInfo("curl import button clicked")
        updateDialogState(null)
        navigate(NavigationDestination.CurlImport)
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
            SelectionMode.HOME_SCREEN_WIDGET_PLACEMENT -> openWidgetSettings(shortcutId)
            SelectionMode.PLUGIN -> returnForPlugin(shortcutId)
            SelectionMode.NORMAL -> Unit
        }
    }

    private suspend fun openWidgetSettings(shortcutId: ShortcutId) {
        val shortcut = getShortcutById(shortcutId) ?: return
        navigate(
            NavigationDestination.Widget.buildRequest(
                shortcutId = shortcut.id,
                shortcutName = shortcut.name,
                shortcutIcon = shortcut.icon,
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

    private suspend fun returnForHomeScreenWidgetPlacement(shortcutId: ShortcutId, showLabel: Boolean, labelColor: String?) {
        val widgetId = initData.widgetId ?: return
        widgetManager.createWidget(widgetId, shortcutId, showLabel, labelColor)
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
        val shortcut = getShortcutById(shortcutId) ?: return
        finish(
            intent = launcherShortcutManager.createShortcutPinIntent(shortcut.toShortcutPlaceholder()),
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
        navigate(
            NavigationDestination.ShortcutEditor.buildRequest(
                categoryId = viewState.activeCategoryId,
                curlCommand = curlCommand,
            )
        )
    }

    private fun getShortcutById(shortcutId: ShortcutId): Shortcut? {
        for (category in categories) {
            for (shortcut in category.shortcuts) {
                if (shortcut.id == shortcutId) {
                    return shortcut
                }
            }
        }
        return null
    }

    fun onWidgetSettingsSubmitted(shortcutId: ShortcutId, showLabel: Boolean, labelColor: String?) = runAction {
        logInfo("Widget settings submitted")
        returnForHomeScreenWidgetPlacement(shortcutId, showLabel, labelColor)
    }

    fun onShortcutEdited() = runAction {
        logInfo("Shortcut edited")
        val categories = categoryRepository.getCategories()
        this@MainViewModel.categories = categories
        updateLauncherSettings(categories)
    }

    fun onPlaceShortcutOnHomeScreen(shortcut: ShortcutPlaceholder) = runAction {
        placeShortcutOnHomeScreen(shortcut)
    }

    fun onRemoveShortcutFromHomeScreen(shortcut: ShortcutPlaceholder) = runAction {
        removeShortcutFromHomeScreen(shortcut)
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

    fun onRestartRequested() = runAction {
        emitEvent(MainEvent.Restart)
    }

    fun onWidgetSettingsCancelled() = runAction {
        finish()
    }

    data class InitData(
        val selectionMode: SelectionMode,
        val initialCategoryId: CategoryId?,
        val widgetId: Int?,
        val importUrl: Uri?,
        val cancelPendingExecutions: Boolean,
    )
}
