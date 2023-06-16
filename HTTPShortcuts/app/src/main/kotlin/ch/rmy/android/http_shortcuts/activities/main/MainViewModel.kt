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
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.about.AboutActivity
import ch.rmy.android.http_shortcuts.activities.editor.ShortcutEditorActivity
import ch.rmy.android.http_shortcuts.activities.importexport.ImportExportActivity
import ch.rmy.android.http_shortcuts.activities.main.models.CategoryItem
import ch.rmy.android.http_shortcuts.activities.main.usecases.LauncherShortcutMapperUseCase
import ch.rmy.android.http_shortcuts.activities.main.usecases.SecondaryLauncherMapperUseCase
import ch.rmy.android.http_shortcuts.activities.main.usecases.ShouldShowChangeLogDialogUseCase
import ch.rmy.android.http_shortcuts.activities.main.usecases.ShouldShowNetworkRestrictionDialogUseCase
import ch.rmy.android.http_shortcuts.activities.main.usecases.ShouldShowRecoveryDialogUseCase
import ch.rmy.android.http_shortcuts.activities.variables.VariablesActivity
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt
import javax.inject.Inject
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class MainViewModel(application: Application) : BaseViewModel<MainViewModel.InitData, MainViewState>(application) {

    @Inject
    lateinit var categoryRepository: CategoryRepository

    @Inject
    lateinit var appRepository: AppRepository

    @Inject
    lateinit var launcherShortcutMapper: LauncherShortcutMapperUseCase

    @Inject
    lateinit var secondaryLauncherMapper: SecondaryLauncherMapperUseCase

    @Inject
    lateinit var temporaryShortcutRepository: TemporaryShortcutRepository

    @Inject
    lateinit var shouldShowRecoveryDialog: ShouldShowRecoveryDialogUseCase

    @Inject
    lateinit var shouldShowChangeLogDialog: ShouldShowChangeLogDialogUseCase

    @Inject
    lateinit var shouldShowNetworkRestrictionDialog: ShouldShowNetworkRestrictionDialogUseCase

    @Inject
    lateinit var executionScheduler: ExecutionScheduler

    @Inject
    lateinit var launcherShortcutManager: LauncherShortcutManager

    @Inject
    lateinit var secondaryLauncherManager: SecondaryLauncherManager

    @Inject
    lateinit var widgetManager: WidgetManager

    @Inject
    lateinit var pendingExecutionsRepository: PendingExecutionsRepository

    @Inject
    lateinit var appOverlayUtil: AppOverlayUtil

    @Inject
    lateinit var variableRepository: VariableRepository

    @Inject
    lateinit var variablePlaceholderProvider: VariablePlaceholderProvider

    @Inject
    lateinit var settings: Settings

    init {
        getApplicationComponent().inject(this)
    }

    private lateinit var categories: List<Category>

    private val selectionMode
        get() = initData.selectionMode

    private val dialogState: MainDialogState?
        get() = currentViewState?.dialogState

    private var activeShortcutId: ShortcutId? = null

    override fun onInitializationStarted(data: InitData) {
        if (data.importUrl != null) {
            openActivity(
                ImportExportActivity.IntentBuilder()
                    .importUrl(data.importUrl)
            )
        }

        viewModelScope.launch {
            this@MainViewModel.categories = categoryRepository.getCategories()
            finalizeInitialization()
        }

        viewModelScope.launch(Dispatchers.Default) {
            // Ensure that the VariablePlaceholderProvider is initialized
            variablePlaceholderProvider.applyVariables(variableRepository.getVariables())
        }
    }

    override fun initViewState() = MainViewState(
        selectionMode = selectionMode,
        categoryItems = getCategoryTabItems(),
        activeCategoryId = initData.initialCategoryId ?: categories.first { !it.hidden }.id,
        isLocked = false,
    )

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

    override fun onInitialized() {
        observeToolbarTitle()
        observeAppLock()

        if (initData.cancelPendingExecutions) {
            viewModelScope.launch {
                pendingExecutionsRepository.removeAllPendingExecutions()
            }
        } else {
            scheduleExecutions()
        }
        updateLauncherSettings(categories)

        when (selectionMode) {
            SelectionMode.NORMAL -> showNormalStartupDialogsIfNeeded()
            SelectionMode.HOME_SCREEN_SHORTCUT_PLACEMENT -> showToast(R.string.instructions_select_shortcut_for_home_screen, long = true)
            SelectionMode.HOME_SCREEN_WIDGET_PLACEMENT -> {
                if (initData.widgetId != null) {
                    setResult(Activity.RESULT_CANCELED, WidgetManager.getIntent(initData.widgetId!!))
                }
                showToast(R.string.instructions_select_shortcut_for_home_screen, long = true)
            }
            SelectionMode.PLUGIN -> showPluginStartupDialogsIfNeeded()
        }
    }

    private fun scheduleExecutions() {
        viewModelScope.launch {
            executionScheduler.schedule()
        }
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

    fun onRecoveryConfirmed() {
        logInfo("Shortcut recovery confirmed")
        val recoveryInfo = (dialogState as? MainDialogState.RecoverShortcut)
            ?.recoveryInfo
            ?: return
        updateDialogState(null)
        emitEvent(
            MainEvent.OpenShortcutEditor(
                ShortcutEditorActivity.IntentBuilder()
                    .runIfNotNull(recoveryInfo.shortcutId) {
                        shortcutId(it)
                    }
                    .runIfNotNull(recoveryInfo.categoryId) {
                        categoryId(it)
                    }
                    .recoveryMode()
            )
        )
    }

    fun onRecoveryDiscarded() {
        logInfo("Shortcut recovery discarded")
        updateDialogState(null)
        launchWithProgressTracking {
            temporaryShortcutRepository.deleteTemporaryShortcut()
        }
    }

    private fun showNetworkRestrictionWarningDialogIfNeeded() {
        if (shouldShowNetworkRestrictionDialog()) {
            updateDialogState(
                MainDialogState.NetworkRestrictionsWarning,
            )
        }
    }

    private fun showPluginStartupDialogsIfNeeded() {
        if (!appOverlayUtil.canDrawOverlays()) {
            updateDialogState(
                MainDialogState.AppOverlayInfo,
            )
        }
    }

    fun onAppOverlayConfigureButtonClicked() {
        updateDialogState(null)
        appOverlayUtil.getSettingsIntent()
            ?.let(::openActivity)
    }

    private fun updateLauncherSettings(categories: List<Category>) {
        launcherShortcutManager.updateAppShortcuts(launcherShortcutMapper(categories))
        secondaryLauncherManager.setSecondaryLauncherVisibility(secondaryLauncherMapper(categories))
    }

    private fun placeShortcutOnHomeScreen(shortcut: ShortcutPlaceholder) {
        if (launcherShortcutManager.supportsPinning()) {
            launcherShortcutManager.pinShortcut(shortcut)
        } else {
            sendBroadcast(IntentUtil.getLegacyShortcutPlacementIntent(context, shortcut, install = true))
            showSnackbar(StringResLocalizable(R.string.shortcut_placed, shortcut.name))
        }
    }

    private fun removeShortcutFromHomeScreen(shortcut: ShortcutPlaceholder) {
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

    fun onSettingsButtonClicked() {
        logInfo("Settings button clicked")
        emitEvent(MainEvent.OpenSettings)
    }

    fun onImportExportButtonClicked() {
        logInfo("Import/export button clicked")
        emitEvent(MainEvent.OpenImportExport)
    }

    fun onAboutButtonClicked() {
        logInfo("About button clicked")
        openActivity(AboutActivity.IntentBuilder())
    }

    fun onCategoriesButtonClicked() {
        logInfo("Categories button clicked")
        openCategoriesEditor()
    }

    private fun openCategoriesEditor() {
        emitEvent(MainEvent.OpenCategories)
    }

    fun onVariablesButtonClicked() {
        logInfo("Variables button clicked")
        openActivity(VariablesActivity.IntentBuilder())
    }

    fun onToolbarTitleClicked() {
        logInfo("Toolbar title clicked")
        doWithViewState { viewState ->
            if (selectionMode == SelectionMode.NORMAL && !viewState.isLocked) {
                showToolbarTitleChangeDialog(viewState.toolbarTitle)
            }
        }
    }

    private fun showToolbarTitleChangeDialog(oldTitle: String) {
        updateDialogState(
            MainDialogState.ChangeTitle(oldTitle),
        )
    }

    fun onCreationDialogOptionSelected(executionType: ShortcutExecutionType) {
        updateDialogState(null)
        doWithViewState { viewState ->
            logInfo("Preparing to open editor for creating shortcut of type $executionType")
            emitEvent(
                MainEvent.OpenShortcutEditor(
                    ShortcutEditorActivity.IntentBuilder()
                        .categoryId(viewState.activeCategoryId)
                        .executionType(executionType)
                )
            )
        }
    }

    fun onCreationDialogHelpButtonClicked() {
        logInfo("Shortcut creation help button clicked")
        openURL(ExternalURLs.SHORTCUTS_DOCUMENTATION)
    }

    fun onCreateShortcutButtonClicked() {
        logInfo("Shortcut creation FAB clicked")
        updateDialogState(
            MainDialogState.ShortcutCreation,
        )
    }

    fun onToolbarTitleChangeSubmitted(newTitle: String) {
        updateDialogState(null)
        launchWithProgressTracking {
            appRepository.setToolbarTitle(newTitle)
            showSnackbar(R.string.message_title_changed)
        }
    }

    fun onActiveCategoryChanged(categoryId: CategoryId) {
        updateViewState {
            copy(activeCategoryId = categoryId)
        }
    }

    fun onUnlockButtonClicked() {
        logInfo("Unlock button clicked")
        updateDialogState(
            MainDialogState.Unlock(),
        )
    }

    fun onAppLocked() {
        showSnackbar(R.string.message_app_locked)
    }

    fun onUnlockDialogSubmitted(password: String) {
        launchWithProgressTracking {
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

    fun onCurlImportOptionSelected() {
        logInfo("curl import button clicked")
        updateDialogState(null)
        emitEvent(MainEvent.OpenCurlImport)
    }

    fun onShortcutCreated(shortcutId: ShortcutId) {
        logInfo("Shortcut created")
        viewModelScope.launch {
            val categories = categoryRepository.getCategories()
            this@MainViewModel.categories = categories
            updateLauncherSettings(categories)
            selectShortcut(shortcutId)
        }
    }

    private fun selectShortcut(shortcutId: ShortcutId) {
        when (selectionMode) {
            SelectionMode.HOME_SCREEN_SHORTCUT_PLACEMENT -> returnForHomeScreenShortcutPlacement(shortcutId)
            SelectionMode.HOME_SCREEN_WIDGET_PLACEMENT -> openWidgetSettings(shortcutId)
            SelectionMode.PLUGIN -> returnForPlugin(shortcutId)
            SelectionMode.NORMAL -> Unit
        }
    }

    private fun openWidgetSettings(shortcutId: ShortcutId) {
        val shortcut = getShortcutById(shortcutId) ?: return
        emitEvent(MainEvent.OpenWidgetSettings(shortcut.toShortcutPlaceholder()))
    }

    private fun returnForHomeScreenShortcutPlacement(shortcutId: ShortcutId) {
        if (launcherShortcutManager.supportsPinning()) {
            activeShortcutId = shortcutId
            updateDialogState(
                MainDialogState.ShortcutPlacement,
            )
        } else {
            placeShortcutOnHomeScreenAndFinish(shortcutId)
        }
    }

    private fun placeShortcutOnHomeScreenAndFinish(shortcutId: ShortcutId) {
        placeOnHomeScreenWithLegacyAndFinish(shortcutId)
    }

    private fun returnForHomeScreenWidgetPlacement(shortcutId: ShortcutId, showLabel: Boolean, labelColor: String?) {
        val widgetId = initData.widgetId ?: return
        viewModelScope.launch {
            widgetManager.createWidget(widgetId, shortcutId, showLabel, labelColor)
            widgetManager.updateWidgets(context, shortcutId)
            finishWithOkResult(
                WidgetManager.getIntent(widgetId)
            )
        }
    }

    fun onShortcutPlacementConfirmed(useLegacyMethod: Boolean) {
        updateDialogState(null)
        val shortcutId = activeShortcutId ?: return
        if (useLegacyMethod) {
            placeOnHomeScreenWithLegacyAndFinish(shortcutId)
        } else {
            placeOnHomeScreenAndFinish(shortcutId)
        }
    }

    private fun placeOnHomeScreenAndFinish(shortcutId: ShortcutId) {
        val shortcut = getShortcutById(shortcutId) ?: return
        finishWithOkResult(launcherShortcutManager.createShortcutPinIntent(shortcut.toShortcutPlaceholder()))
    }

    private fun placeOnHomeScreenWithLegacyAndFinish(shortcutId: ShortcutId) {
        val shortcut = getShortcutById(shortcutId) ?: return
        finishWithOkResult(IntentUtil.getLegacyShortcutPlacementIntent(context, shortcut.toShortcutPlaceholder(), install = true))
    }

    private fun returnForPlugin(shortcutId: ShortcutId) {
        val shortcut = getShortcutById(shortcutId) ?: return
        finishWithOkResult(
            createIntent {
                putExtra(MainActivity.EXTRA_SELECTION_ID, shortcut.id)
                putExtra(MainActivity.EXTRA_SELECTION_NAME, shortcut.name)
            },
        )
    }

    fun onCurlCommandSubmitted(curlCommand: CurlCommand) {
        logInfo("curl command submitted")
        doWithViewState { viewState ->
            emitEvent(
                MainEvent.OpenShortcutEditor(
                    ShortcutEditorActivity.IntentBuilder()
                        .categoryId(viewState.activeCategoryId)
                        .curlCommand(curlCommand)
                )
            )
        }
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

    fun onWidgetSettingsSubmitted(shortcutId: ShortcutId, showLabel: Boolean, labelColor: String?) {
        logInfo("Widget settings submitted")
        returnForHomeScreenWidgetPlacement(shortcutId, showLabel, labelColor)
    }

    fun onShortcutEdited() {
        logInfo("Shortcut edited")
        viewModelScope.launch {
            val categories = categoryRepository.getCategories()
            this@MainViewModel.categories = categories
            updateLauncherSettings(categories)
        }
    }

    fun onPlaceShortcutOnHomeScreen(shortcut: ShortcutPlaceholder) {
        placeShortcutOnHomeScreen(shortcut)
    }

    fun onRemoveShortcutFromHomeScreen(shortcut: ShortcutPlaceholder) {
        removeShortcutFromHomeScreen(shortcut)
    }

    fun onSelectShortcut(shortcutId: ShortcutId) {
        logInfo("Shortcut selected")
        selectShortcut(shortcutId)
    }

    fun onDialogDismissed() {
        if (dialogState is MainDialogState.ChangeLog && shouldShowNetworkRestrictionDialog()) {
            updateDialogState(
                MainDialogState.NetworkRestrictionsWarning,
            )
        } else {
            updateDialogState(null)
        }
    }

    private fun updateDialogState(dialogState: MainDialogState?) {
        updateViewState {
            copy(dialogState = dialogState)
        }
    }

    fun onBackButtonPressed() {
        finish()
        ActivityCloser.onMainActivityClosed()
    }

    fun onRestartRequested() {
        emitEvent(MainEvent.Restart)
    }

    fun onWidgetSettingsCancelled() {
        finish()
    }

    fun onReopenSettingsRequested() {
        emitEvent(MainEvent.ReopenSettings)
    }

    data class InitData(
        val selectionMode: SelectionMode,
        val initialCategoryId: CategoryId?,
        val widgetId: Int?,
        val importUrl: Uri?,
        val cancelPendingExecutions: Boolean,
    )
}
