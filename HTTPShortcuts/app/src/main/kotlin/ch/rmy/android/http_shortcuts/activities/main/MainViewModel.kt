package ch.rmy.android.http_shortcuts.activities.main

import android.app.Activity
import android.app.Application
import android.content.Intent
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.createIntent
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.mapIf
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.EventBridge
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.categories.CategoriesActivity
import ch.rmy.android.http_shortcuts.activities.editor.ShortcutEditorActivity
import ch.rmy.android.http_shortcuts.activities.main.usecases.GetNetworkRestrictionDialogUseCase
import ch.rmy.android.http_shortcuts.activities.main.usecases.GetShortcutCreationDialogUseCase
import ch.rmy.android.http_shortcuts.activities.main.usecases.GetShortcutPlacementDialogUseCase
import ch.rmy.android.http_shortcuts.activities.main.usecases.GetToolbarTitleChangeDialogUseCase
import ch.rmy.android.http_shortcuts.activities.main.usecases.GetUnlockDialogUseCase
import ch.rmy.android.http_shortcuts.activities.main.usecases.ShouldShowChangeLogDialogUseCase
import ch.rmy.android.http_shortcuts.activities.main.usecases.ShouldShowNetworkRestrictionDialogUseCase
import ch.rmy.android.http_shortcuts.activities.misc.CurlImportActivity
import ch.rmy.android.http_shortcuts.activities.settings.ImportExportActivity
import ch.rmy.android.http_shortcuts.activities.settings.SettingsActivity
import ch.rmy.android.http_shortcuts.activities.settings.about.AboutActivity
import ch.rmy.android.http_shortcuts.activities.variables.VariablesActivity
import ch.rmy.android.http_shortcuts.activities.widget.WidgetSettingsActivity
import ch.rmy.android.http_shortcuts.data.domains.app.AppRepository
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryRepository
import ch.rmy.android.http_shortcuts.data.dtos.LauncherShortcut
import ch.rmy.android.http_shortcuts.data.enums.SelectionMode
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.toLauncherShortcut
import ch.rmy.android.http_shortcuts.usecases.GetChangeLogDialogUseCase
import ch.rmy.android.http_shortcuts.utils.ExternalURLs
import ch.rmy.android.http_shortcuts.utils.IntentUtil
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import ch.rmy.android.http_shortcuts.utils.Settings
import ch.rmy.android.http_shortcuts.widget.WidgetManager
import ch.rmy.curlcommand.CurlCommand
import io.reactivex.Single
import org.mindrot.jbcrypt.BCrypt

class MainViewModel(application: Application) : BaseViewModel<MainViewModel.InitData, MainViewState>(application), WithDialog {

    private val categoryRepository: CategoryRepository = CategoryRepository()
    private val appRepository: AppRepository = AppRepository()
    private val launcherShortcutMapper: LauncherShortcutMapper = LauncherShortcutMapper()
    private val eventBridge = EventBridge(ChildViewModelEvent::class.java)
    private val shouldShowChangeLogDialog = ShouldShowChangeLogDialogUseCase(context)
    private val settings = Settings(context)
    private val getChangeLogDialog = GetChangeLogDialogUseCase(settings)
    private val getToolbarTitleChangeDialog = GetToolbarTitleChangeDialogUseCase()
    private val getShortcutPlacementDialog = GetShortcutPlacementDialogUseCase()
    private val getUnlockDialog = GetUnlockDialogUseCase()
    private val getShortcutCreationDialog = GetShortcutCreationDialogUseCase()
    private val getNetworkRestrictionDialog = GetNetworkRestrictionDialogUseCase(settings)
    private val shouldShowNetworkRestrictionDialog = ShouldShowNetworkRestrictionDialogUseCase(context, settings)

    private lateinit var categories: List<Category>

    private val selectionMode
        get() = initData.selectionMode

    override var dialogState: DialogState?
        get() = currentViewState.dialogState
        set(value) {
            updateViewState {
                copy(dialogState = value)
            }
        }

    override fun onInitializationStarted(data: InitData) {
        categoryRepository.getCategories()
            .subscribe { categories ->
                this.categories = categories
                finalizeInitialization()
            }
            .attachTo(destroyer)
    }

    override fun initViewState() = MainViewState(
        selectionMode = selectionMode,
        categoryTabItems = getCategoryTabItems(),
        activeCategoryId = initData.initialCategoryId ?: categories.first().id,
        isInMovingMode = false,
        isLocked = false,
    )

    private fun getCategoryTabItems() =
        categories
            .mapIf(selectionMode == SelectionMode.NORMAL) {
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
        observeChildViewModelEvents()
        observeToolbarTitle()
        observeAppLock()

        emitEvent(MainEvent.ScheduleExecutions)
        updateLauncherShortcuts()

        if (selectionMode === SelectionMode.NORMAL) {
            showStartupDialogsIfNeeded()
        } else {
            if (selectionMode == SelectionMode.HOME_SCREEN_WIDGET_PLACEMENT && initData.widgetId != null) {
                emitEvent(ViewModelEvent.SetResult(Activity.RESULT_CANCELED, WidgetManager.getIntent(initData.widgetId!!)))
            }
            if (
                selectionMode == SelectionMode.HOME_SCREEN_WIDGET_PLACEMENT ||
                selectionMode == SelectionMode.HOME_SCREEN_SHORTCUT_PLACEMENT

            ) {
                showToast(R.string.instructions_select_shortcut_for_home_screen, long = true)
            }
        }
    }

    private fun showStartupDialogsIfNeeded() {
        if (shouldShowChangeLogDialog()) {
            dialogState = getChangeLogDialog(whatsNew = true)
        } else {
            showNetworkRestrictionWarningDialogIfNeeded()
        }
    }

    private fun showNetworkRestrictionWarningDialogIfNeeded() {
        if (shouldShowNetworkRestrictionDialog()) {
            dialogState = getNetworkRestrictionDialog()
        }
    }

    private fun observeChildViewModelEvents() {
        eventBridge.events.subscribe(::handleChildViewModelEvent)
            .attachTo(destroyer)
    }

    private fun handleChildViewModelEvent(event: ChildViewModelEvent) {
        when (event) {
            is ChildViewModelEvent.MovingModeChanged -> {
                updateViewState {
                    copy(isInMovingMode = event.enabled)
                }
            }
            is ChildViewModelEvent.ShortcutEdited -> {
                categoryRepository.getCategories()
                    .subscribe { categories ->
                        this.categories = categories
                        updateLauncherShortcuts()
                    }
                    .attachTo(destroyer)
            }
            is ChildViewModelEvent.PlaceShortcutOnHomeScreen -> placeShortcutOnHomeScreen(event.shortcut)
            is ChildViewModelEvent.RemoveShortcutFromHomeScreen -> removeShortcutFromHomeScreen(event.shortcut)
            is ChildViewModelEvent.SelectShortcut -> selectShortcut(event.shortcutId)
        }
    }

    private fun updateLauncherShortcuts() {
        emitEvent(MainEvent.UpdateLauncherShortcuts(launcherShortcutMapper(categories)))
    }

    private fun placeShortcutOnHomeScreen(shortcut: LauncherShortcut) {
        if (LauncherShortcutManager.supportsPinning(context)) {
            LauncherShortcutManager.pinShortcut(context, shortcut)
        } else {
            sendBroadcast(IntentUtil.getLegacyShortcutPlacementIntent(context, shortcut, install = true))
            showSnackbar(StringResLocalizable(R.string.shortcut_placed, shortcut.name))
        }
    }

    private fun removeShortcutFromHomeScreen(shortcut: LauncherShortcut) {
        sendBroadcast(IntentUtil.getLegacyShortcutPlacementIntent(context, shortcut, install = false))
    }

    private fun observeToolbarTitle() {
        appRepository.getObservableToolbarTitle()
            .subscribe { toolbarTitle ->
                updateViewState {
                    copy(toolbarTitle = toolbarTitle)
                }
            }
            .attachTo(destroyer)
    }

    private fun observeAppLock() {
        appRepository.getObservableLock()
            .subscribe { optionalLock ->
                updateViewState {
                    copy(isLocked = optionalLock.value != null)
                }
            }
            .attachTo(destroyer)
    }

    fun onSettingsButtonClicked() {
        openActivity(SettingsActivity.IntentBuilder(), MainActivity.REQUEST_SETTINGS)
    }

    fun onImportExportButtonClicked() {
        openActivity(ImportExportActivity.IntentBuilder(), MainActivity.REQUEST_IMPORT_EXPORT)
    }

    fun onAboutButtonClicked() {
        openActivity(AboutActivity.IntentBuilder())
    }

    fun onCategoriesButtonClicked() {
        openCategoriesEditor()
    }

    private fun openCategoriesEditor() {
        openActivity(CategoriesActivity.IntentBuilder(), MainActivity.REQUEST_CATEGORIES)
    }

    fun onVariablesButtonClicked() {
        openActivity(VariablesActivity.IntentBuilder())
    }

    fun onTabLongClicked() {
        if (selectionMode == SelectionMode.NORMAL && !currentViewState.isLocked) {
            openCategoriesEditor()
        }
    }

    fun onToolbarTitleClicked() {
        if (selectionMode == SelectionMode.NORMAL && !currentViewState.isLocked) {
            showToolbarTitleChangeDialog(currentViewState.toolbarTitle)
        }
    }

    private fun showToolbarTitleChangeDialog(oldTitle: String) {
        dialogState = getToolbarTitleChangeDialog(this, oldTitle)
    }

    fun onCreationDialogOptionSelected(executionType: ShortcutExecutionType) {
        logInfo("Preparing to open editor for creating shortcut of type $executionType")
        openActivity(
            ShortcutEditorActivity.IntentBuilder()
                .categoryId(currentViewState.activeCategoryId)
                .executionType(executionType),
            requestCode = MainActivity.REQUEST_CREATE_SHORTCUT,
        )
    }

    fun onCreationDialogHelpButtonClicked() {
        openURL(ExternalURLs.SHORTCUTS_DOCUMENTATION)
    }

    fun onCreateShortcutButtonClicked() {
        dialogState = getShortcutCreationDialog(this)
    }

    fun onToolbarTitleChangeSubmitted(newTitle: String) {
        performOperation(appRepository.setToolbarTitle(newTitle)) {
            showSnackbar(R.string.message_title_changed)
        }
    }

    fun onSwitchedToCategory(position: Int) {
        val activateCategoryId = categories
            .mapIf(selectionMode == SelectionMode.NORMAL) {
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
        performOperation(
            appRepository.getLock()
                .flatMap { optionalLock ->
                    val passwordHash = optionalLock.value?.passwordHash
                    if (passwordHash != null && BCrypt.checkpw(password, passwordHash)) {
                        appRepository.removeLock()
                            .toSingleDefault(true)
                    } else {
                        Single.just(passwordHash == null)
                    }
                }
                .doOnSuccess { unlocked ->
                    if (unlocked) {
                        showSnackbar(R.string.message_app_unlocked)
                    } else {
                        showUnlockDialog(StringResLocalizable(R.string.dialog_text_unlock_app_retry))
                    }
                }
                .ignoreElement()
        )
    }

    fun onCurlImportOptionSelected() {
        openActivity(
            CurlImportActivity.IntentBuilder(),
            requestCode = MainActivity.REQUEST_CREATE_SHORTCUT_FROM_CURL,
        )
    }

    fun onShortcutCreated(shortcutId: String) {
        categoryRepository.getCategories()
            .subscribe { categories ->
                this.categories = categories
                emitEvent(MainEvent.UpdateLauncherShortcuts(launcherShortcutMapper(categories)))
                selectShortcut(shortcutId)
            }
            .attachTo(destroyer)
    }

    private fun selectShortcut(shortcutId: String) {
        when (selectionMode) {
            SelectionMode.HOME_SCREEN_SHORTCUT_PLACEMENT -> returnForHomeScreenShortcutPlacement(shortcutId)
            SelectionMode.HOME_SCREEN_WIDGET_PLACEMENT -> openWidgetSettings(shortcutId)
            SelectionMode.PLUGIN -> returnForPlugin(shortcutId)
            SelectionMode.NORMAL -> Unit
        }
    }

    private fun openWidgetSettings(shortcutId: String) {
        val shortcut = getShortcutById(shortcutId) ?: return
        openActivity(
            WidgetSettingsActivity.IntentBuilder()
                .shortcut(shortcut.toLauncherShortcut()),
            requestCode = MainActivity.REQUEST_WIDGET_SETTINGS,
        )
    }

    private fun returnForHomeScreenShortcutPlacement(shortcutId: String) {
        if (LauncherShortcutManager.supportsPinning(context)) {
            dialogState = getShortcutPlacementDialog(this, shortcutId)
        } else {
            placeShortcutOnHomeScreenAndFinish(shortcutId)
        }
    }

    private fun placeShortcutOnHomeScreenAndFinish(shortcutId: String) {
        placeOnHomeScreenWithLegacyAndFinish(shortcutId)
    }

    private fun returnForHomeScreenWidgetPlacement(shortcutId: String, showLabel: Boolean, labelColor: String?) {
        val widgetId = initData.widgetId ?: return
        val widgetManager = WidgetManager()
        widgetManager
            .createWidget(widgetId, shortcutId, showLabel, labelColor)
            .andThen(widgetManager.updateWidgets(context, shortcutId))
            .subscribe {
                finishWithOkResult(
                    WidgetManager.getIntent(widgetId)
                )
            }
            .attachTo(destroyer)
    }

    fun onShortcutPlacementConfirmed(shortcutId: String, useLegacyMethod: Boolean) {
        if (useLegacyMethod) {
            placeOnHomeScreenWithLegacyAndFinish(shortcutId)
        } else {
            placeOnHomeScreenAndFinish(shortcutId)
        }
    }

    private fun placeOnHomeScreenAndFinish(shortcutId: String) {
        val shortcut = getShortcutById(shortcutId) ?: return
        finishWithOkResult(LauncherShortcutManager.createShortcutPinIntent(context, shortcut.toLauncherShortcut()))
    }

    private fun placeOnHomeScreenWithLegacyAndFinish(shortcutId: String) {
        val shortcut = getShortcutById(shortcutId) ?: return
        finishWithOkResult(IntentUtil.getLegacyShortcutPlacementIntent(context, shortcut.toLauncherShortcut(), install = true))
    }

    private fun returnForPlugin(shortcutId: String) {
        val shortcut = getShortcutById(shortcutId) ?: return
        finishWithOkResult(
            createIntent {
                putExtra(MainActivity.EXTRA_SELECTION_ID, shortcut.id)
                putExtra(MainActivity.EXTRA_SELECTION_NAME, shortcut.name)
            },
        )
    }

    fun onCurlCommandSubmitted(curlCommand: CurlCommand) {
        openActivity(
            ShortcutEditorActivity.IntentBuilder()
                .categoryId(currentViewState.activeCategoryId)
                .curlCommand(curlCommand),
            requestCode = MainActivity.REQUEST_CREATE_SHORTCUT,
        )
    }

    private fun getShortcutById(shortcutId: String): Shortcut? {
        for (category in categories) {
            for (shortcut in category.shortcuts) {
                if (shortcut.id == shortcutId) {
                    return shortcut
                }
            }
        }
        return null
    }

    fun onWidgetSettingsSubmitted(shortcutId: String, showLabel: Boolean, labelColor: String?) {
        returnForHomeScreenWidgetPlacement(shortcutId, showLabel, labelColor)
    }

    private fun finishWithOkResult(intent: Intent) {
        finish(
            result = Activity.RESULT_OK,
            intent = intent,
        )
    }

    override fun onDialogDismissed(dialogState: DialogState) {
        atomicallyUpdateViewState {
            super.onDialogDismissed(dialogState)
            if (dialogState.id == GetChangeLogDialogUseCase.DIALOG_ID) {
                showNetworkRestrictionWarningDialogIfNeeded()
            }
        }
    }

    data class InitData(
        val selectionMode: SelectionMode,
        val initialCategoryId: String?,
        val widgetId: Int?,
    )
}
