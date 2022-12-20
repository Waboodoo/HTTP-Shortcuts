package ch.rmy.android.http_shortcuts.activities.main

import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.createIntent
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.framework.ui.IntentBuilder
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.editor.ShortcutEditorActivity
import ch.rmy.android.http_shortcuts.activities.history.HistoryActivity
import ch.rmy.android.http_shortcuts.activities.main.models.CategoryTabItem
import ch.rmy.android.http_shortcuts.activities.main.models.RecoveryInfo
import ch.rmy.android.http_shortcuts.activities.main.usecases.GetAppOverlayDialogUseCase
import ch.rmy.android.http_shortcuts.activities.main.usecases.GetNetworkRestrictionDialogUseCase
import ch.rmy.android.http_shortcuts.activities.main.usecases.GetRecoveryDialogUseCase
import ch.rmy.android.http_shortcuts.activities.main.usecases.GetShortcutCreationDialogUseCase
import ch.rmy.android.http_shortcuts.activities.main.usecases.GetShortcutPlacementDialogUseCase
import ch.rmy.android.http_shortcuts.activities.main.usecases.GetUnlockDialogUseCase
import ch.rmy.android.http_shortcuts.activities.main.usecases.LauncherShortcutMapperUseCase
import ch.rmy.android.http_shortcuts.activities.main.usecases.SecondaryLauncherMapperUseCase
import ch.rmy.android.http_shortcuts.activities.main.usecases.ShouldShowChangeLogDialogUseCase
import ch.rmy.android.http_shortcuts.activities.main.usecases.ShouldShowNetworkRestrictionDialogUseCase
import ch.rmy.android.http_shortcuts.activities.main.usecases.ShouldShowRecoveryDialogUseCase
import ch.rmy.android.http_shortcuts.activities.settings.about.AboutActivity
import ch.rmy.android.http_shortcuts.activities.settings.importexport.ImportExportActivity
import ch.rmy.android.http_shortcuts.activities.variables.VariablesActivity
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.app.AppRepository
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryRepository
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.PendingExecutionsRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.dtos.LauncherShortcut
import ch.rmy.android.http_shortcuts.data.enums.SelectionMode
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.models.CategoryModel
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.extensions.toLauncherShortcut
import ch.rmy.android.http_shortcuts.scheduling.ExecutionScheduler
import ch.rmy.android.http_shortcuts.usecases.GetChangeLogDialogUseCase
import ch.rmy.android.http_shortcuts.usecases.GetToolbarTitleChangeDialogUseCase
import ch.rmy.android.http_shortcuts.utils.AppOverlayUtil
import ch.rmy.android.http_shortcuts.utils.ExternalURLs
import ch.rmy.android.http_shortcuts.utils.IntentUtil
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import ch.rmy.android.http_shortcuts.utils.SecondaryLauncherManager
import ch.rmy.android.http_shortcuts.widget.WidgetManager
import ch.rmy.curlcommand.CurlCommand
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt
import javax.inject.Inject

class MainViewModel(application: Application) : BaseViewModel<MainViewModel.InitData, MainViewState>(application), WithDialog {

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
    lateinit var getChangeLogDialog: GetChangeLogDialogUseCase

    @Inject
    lateinit var getToolbarTitleChangeDialog: GetToolbarTitleChangeDialogUseCase

    @Inject
    lateinit var getShortcutPlacementDialog: GetShortcutPlacementDialogUseCase

    @Inject
    lateinit var getUnlockDialog: GetUnlockDialogUseCase

    @Inject
    lateinit var getShortcutCreationDialog: GetShortcutCreationDialogUseCase

    @Inject
    lateinit var getNetworkRestrictionDialog: GetNetworkRestrictionDialogUseCase

    @Inject
    lateinit var shouldShowNetworkRestrictionDialog: ShouldShowNetworkRestrictionDialogUseCase

    @Inject
    lateinit var executionScheduler: ExecutionScheduler

    @Inject
    lateinit var launcherShortcutManager: LauncherShortcutManager

    @Inject
    lateinit var secondaryLauncherManager: SecondaryLauncherManager

    @Inject
    lateinit var getRecoveryDialog: GetRecoveryDialogUseCase

    @Inject
    lateinit var widgetManager: WidgetManager

    @Inject
    lateinit var pendingExecutionsRepository: PendingExecutionsRepository

    @Inject
    lateinit var appOverlayUtil: AppOverlayUtil

    @Inject
    lateinit var getAppOverlayDialog: GetAppOverlayDialogUseCase

    init {
        getApplicationComponent().inject(this)
    }

    private lateinit var categories: List<CategoryModel>

    private val selectionMode
        get() = initData.selectionMode

    override var dialogState: DialogState?
        get() = currentViewState?.dialogState
        set(value) {
            updateViewState {
                copy(dialogState = value)
            }
        }

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
    }

    override fun initViewState() = MainViewState(
        selectionMode = selectionMode,
        categoryTabItems = getCategoryTabItems(),
        activeCategoryId = initData.initialCategoryId ?: categories.first { !it.hidden }.id,
        isInMovingMode = false,
        isLocked = false,
    )

    private fun getCategoryTabItems() =
        categories
            .runIf(selectionMode == SelectionMode.NORMAL) {
                filterNot { it.hidden }
            }
            .map { category ->
                CategoryTabItem(
                    categoryId = category.id,
                    name = category.name,
                    layoutType = category.categoryLayoutType,
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
            val recoveryInfo = shouldShowRecoveryDialog()
            if (recoveryInfo != null) {
                showRecoveryDialog(recoveryInfo)
            } else {
                if (shouldShowChangeLogDialog()) {
                    dialogState = getChangeLogDialog(whatsNew = true)
                } else {
                    showNetworkRestrictionWarningDialogIfNeeded()
                }
            }
        }
    }

    private fun showRecoveryDialog(recoveryInfo: RecoveryInfo) {
        dialogState = getRecoveryDialog(
            recoveryInfo,
            onRecover = {
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
            },
            onDiscard = {
                launchWithProgressTracking {
                    temporaryShortcutRepository.deleteTemporaryShortcut()
                }
            },
        )
    }

    private fun showNetworkRestrictionWarningDialogIfNeeded() {
        if (shouldShowNetworkRestrictionDialog()) {
            dialogState = getNetworkRestrictionDialog()
        }
    }

    private fun showPluginStartupDialogsIfNeeded() {
        if (!appOverlayUtil.canDrawOverlays()) {
            dialogState = getAppOverlayDialog {
                val intent = appOverlayUtil.getSettingsIntent() ?: return@getAppOverlayDialog
                openActivity(object : IntentBuilder {
                    override fun build(context: Context) =
                        intent
                })
            }
        }
    }

    private fun updateLauncherSettings(categories: List<CategoryModel>) {
        launcherShortcutManager.updateAppShortcuts(launcherShortcutMapper(categories))
        secondaryLauncherManager.setSecondaryLauncherVisibility(secondaryLauncherMapper(categories))
    }

    private fun placeShortcutOnHomeScreen(shortcut: LauncherShortcut) {
        if (launcherShortcutManager.supportsPinning()) {
            launcherShortcutManager.pinShortcut(shortcut)
        } else {
            sendBroadcast(IntentUtil.getLegacyShortcutPlacementIntent(context, shortcut, install = true))
            showSnackbar(StringResLocalizable(R.string.shortcut_placed, shortcut.name))
        }
    }

    private fun removeShortcutFromHomeScreen(shortcut: LauncherShortcut) {
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
        emitEvent(MainEvent.OpenSettings)
    }

    fun onImportExportButtonClicked() {
        emitEvent(MainEvent.OpenImportExport)
    }

    fun onHistoryButtonClicked() {
        openActivity(HistoryActivity.IntentBuilder())
    }

    fun onAboutButtonClicked() {
        openActivity(AboutActivity.IntentBuilder())
    }

    fun onCategoriesButtonClicked() {
        openCategoriesEditor()
    }

    private fun openCategoriesEditor() {
        emitEvent(MainEvent.OpenCategories)
    }

    fun onVariablesButtonClicked() {
        openActivity(VariablesActivity.IntentBuilder())
    }

    fun onTabLongClicked() {
        doWithViewState { viewState ->
            if (selectionMode == SelectionMode.NORMAL && !viewState.isLocked) {
                openCategoriesEditor()
            }
        }
    }

    fun onToolbarTitleClicked() {
        doWithViewState { viewState ->
            if (selectionMode == SelectionMode.NORMAL && !viewState.isLocked) {
                showToolbarTitleChangeDialog(viewState.toolbarTitle)
            }
        }
    }

    private fun showToolbarTitleChangeDialog(oldTitle: String) {
        dialogState = getToolbarTitleChangeDialog(::onToolbarTitleChangeSubmitted, oldTitle)
    }

    fun onCreationDialogOptionSelected(executionType: ShortcutExecutionType) {
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
        openURL(ExternalURLs.SHORTCUTS_DOCUMENTATION)
    }

    fun onCreateShortcutButtonClicked() {
        dialogState = getShortcutCreationDialog(this)
    }

    private fun onToolbarTitleChangeSubmitted(newTitle: String) {
        launchWithProgressTracking {
            appRepository.setToolbarTitle(newTitle)
            showSnackbar(R.string.message_title_changed)
        }
    }

    fun onSwitchedToCategory(position: Int) {
        val activateCategoryId = categories
            .runIf(selectionMode == SelectionMode.NORMAL) {
                filterNot { it.hidden }
            }
            .getOrNull(position)?.id ?: return
        updateViewState {
            copy(activeCategoryId = activateCategoryId)
        }
    }

    fun onUnlockButtonClicked() {
        showUnlockDialog(StringResLocalizable(R.string.dialog_text_unlock_app))
    }

    private fun showUnlockDialog(message: Localizable) {
        dialogState = getUnlockDialog(this, message)
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
                showSnackbar(R.string.message_app_unlocked)
            } else {
                showUnlockDialog(StringResLocalizable(R.string.dialog_text_unlock_app_retry))
            }
        }
    }

    fun onCurlImportOptionSelected() {
        emitEvent(MainEvent.OpenCurlImport)
    }

    fun onShortcutCreated(shortcutId: ShortcutId) {
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
        emitEvent(MainEvent.OpenWidgetSettings(shortcut.toLauncherShortcut()))
    }

    private fun returnForHomeScreenShortcutPlacement(shortcutId: ShortcutId) {
        if (launcherShortcutManager.supportsPinning()) {
            dialogState = getShortcutPlacementDialog(this, shortcutId)
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

    fun onShortcutPlacementConfirmed(shortcutId: ShortcutId, useLegacyMethod: Boolean) {
        if (useLegacyMethod) {
            placeOnHomeScreenWithLegacyAndFinish(shortcutId)
        } else {
            placeOnHomeScreenAndFinish(shortcutId)
        }
    }

    private fun placeOnHomeScreenAndFinish(shortcutId: ShortcutId) {
        val shortcut = getShortcutById(shortcutId) ?: return
        finishWithOkResult(launcherShortcutManager.createShortcutPinIntent(shortcut.toLauncherShortcut()))
    }

    private fun placeOnHomeScreenWithLegacyAndFinish(shortcutId: ShortcutId) {
        val shortcut = getShortcutById(shortcutId) ?: return
        finishWithOkResult(IntentUtil.getLegacyShortcutPlacementIntent(context, shortcut.toLauncherShortcut(), install = true))
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

    private fun getShortcutById(shortcutId: ShortcutId): ShortcutModel? {
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
        returnForHomeScreenWidgetPlacement(shortcutId, showLabel, labelColor)
    }

    override fun onDialogDismissed(dialogState: DialogState) {
        atomicallyUpdateViewState {
            super.onDialogDismissed(dialogState)
            if (dialogState.id == GetChangeLogDialogUseCase.DIALOG_ID) {
                showNetworkRestrictionWarningDialogIfNeeded()
            }
        }
    }

    fun onMovingModeChanged(enabled: Boolean) {
        updateViewState {
            copy(isInMovingMode = enabled)
        }
    }

    fun onShortcutEdited() {
        viewModelScope.launch {
            val categories = categoryRepository.getCategories()
            this@MainViewModel.categories = categories
            updateLauncherSettings(categories)
        }
    }

    fun onPlaceShortcutOnHomeScreen(shortcut: LauncherShortcut) {
        placeShortcutOnHomeScreen(shortcut)
    }

    fun onRemoveShortcutFromHomeScreen(shortcut: LauncherShortcut) {
        removeShortcutFromHomeScreen(shortcut)
    }

    fun onSelectShortcut(shortcutId: ShortcutId) {
        selectShortcut(shortcutId)
    }

    data class InitData(
        val selectionMode: SelectionMode,
        val initialCategoryId: CategoryId?,
        val widgetId: Int?,
        val importUrl: Uri?,
        val cancelPendingExecutions: Boolean,
    )
}
