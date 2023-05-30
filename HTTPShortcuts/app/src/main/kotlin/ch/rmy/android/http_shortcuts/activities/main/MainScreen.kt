package ch.rmy.android.http_shortcuts.activities.main

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.launch
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.categories.CategoriesActivity
import ch.rmy.android.http_shortcuts.activities.curl_import.CurlImportActivity
import ch.rmy.android.http_shortcuts.activities.editor.ShortcutEditorActivity
import ch.rmy.android.http_shortcuts.activities.importexport.ImportExportActivity
import ch.rmy.android.http_shortcuts.activities.settings.SettingsActivity
import ch.rmy.android.http_shortcuts.activities.widget.WidgetSettingsActivity
import ch.rmy.android.http_shortcuts.components.EventHandler
import ch.rmy.android.http_shortcuts.components.FloatingAddButton
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.ToolbarIcon
import ch.rmy.android.http_shortcuts.components.bindViewModel
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.enums.SelectionMode

@Composable
fun MainScreen(
    selectionMode: SelectionMode,
    initialCategoryId: CategoryId?,
    widgetId: Int?,
    importUrl: Uri?,
    cancelPendingExecutions: Boolean,
) {
    val (viewModel, state) = bindViewModel<MainViewModel.InitData, MainViewState, MainViewModel>(
        MainViewModel.InitData(
            selectionMode = selectionMode,
            initialCategoryId = initialCategoryId,
            widgetId = widgetId,
            importUrl = importUrl,
            cancelPendingExecutions = cancelPendingExecutions,
        )
    )

    BackHandler {
        viewModel.onBackButtonPressed()
    }

    val openSettings = rememberLauncherForActivityResult(SettingsActivity.OpenSettings) { result ->
        if (result.themeChanged) {
            viewModel.onReopenSettingsRequested()
        } else if (result.appLocked) {
            viewModel.onAppLocked()
        }
    }

    val openCurlImport = rememberLauncherForActivityResult(CurlImportActivity.ImportFromCurl) { curlCommand ->
        curlCommand?.let(viewModel::onCurlCommandSubmitted)
    }

    val openWidgetSettings = rememberLauncherForActivityResult(WidgetSettingsActivity.OpenWidgetSettings) { result ->
        if (result != null) {
            viewModel.onWidgetSettingsSubmitted(
                shortcutId = result.shortcutId,
                showLabel = result.showLabel,
                labelColor = result.labelColor,
            )
        } else {
            viewModel.onWidgetSettingsCancelled()
        }
    }

    val openShortcutEditor = rememberLauncherForActivityResult(ShortcutEditorActivity.OpenShortcutEditor) { shortcutId ->
        shortcutId?.let(viewModel::onShortcutCreated)
    }

    val openCategories = rememberLauncherForActivityResult(CategoriesActivity.OpenCategories) { categoriesChanged ->
        if (categoriesChanged) {
            viewModel.onRestartRequested()
        }
    }

    val openImportExport = rememberLauncherForActivityResult(ImportExportActivity.OpenImportExport) { categoriesChanged ->
        if (categoriesChanged) {
            viewModel.onRestartRequested()
        }
    }

    EventHandler { event ->
        when (event) {
            is MainEvent.OpenCurlImport -> consume {
                openCurlImport.launch()
            }
            is MainEvent.OpenWidgetSettings -> consume {
                openWidgetSettings.launch {
                    shortcut(event.shortcut)
                }
            }
            is MainEvent.OpenShortcutEditor -> consume {
                openShortcutEditor.launch { event.intentBuilder }
            }
            is MainEvent.OpenCategories -> consume {
                openCategories.launch()
            }
            is MainEvent.OpenSettings -> consume {
                openSettings.launch()
            }
            is MainEvent.OpenImportExport -> consume {
                openImportExport.launch()
            }
            else -> false
        }
    }

    SimpleScaffold(
        viewState = state,
        title = state?.run { toolbarTitle.ifEmpty { stringResource(R.string.app_name) } } ?: "",
        backButton = null,
        actions = { viewState ->
            if (viewState.isLocked) {
                ToolbarIcon(
                    Icons.Outlined.Lock,
                    contentDescription = stringResource(R.string.menu_action_unlock_app),
                    onClick = viewModel::onUnlockButtonClicked,
                )
            } else {
                MainMenu(
                    onCategoriesButtonClicked = viewModel::onCategoriesButtonClicked,
                    onVariablesButtonClicked = viewModel::onVariablesButtonClicked,
                    onImportExportButtonClicked = viewModel::onImportExportButtonClicked,
                    onSettingsButtonClicked = viewModel::onSettingsButtonClicked,
                    onAboutButtonClicked = viewModel::onAboutButtonClicked,
                )
            }
        },
        onTitleClicked = if (state?.isLocked == false) {
            viewModel::onToolbarTitleClicked
        } else null,
        floatingActionButton = {
            AnimatedVisibility(
                visible = state?.isCreateButtonVisible == true,
                enter = scaleIn(),
                exit = scaleOut(),
            ) {
                FloatingAddButton(onClick = viewModel::onCreateShortcutButtonClicked)
            }
        },
    ) { viewState ->
        MainContent(
            categoryItems = viewState.categoryItems,
            activeCategoryId = viewState.activeCategoryId,
            selectionMode = viewState.selectionMode,
            onActiveCategoryIdChanged = viewModel::onActiveCategoryChanged,
            onShortcutEdited = viewModel::onShortcutEdited,
            onPlaceShortcutOnHomeScreen = viewModel::onPlaceShortcutOnHomeScreen,
            onRemoveShortcutFromHomeScreen = viewModel::onRemoveShortcutFromHomeScreen,
            onSelectShortcut = viewModel::onSelectShortcut,
        )
    }

    MainDialogs(
        dialogState = state?.dialogState,
        onChangelogPermanentlyHiddenChanged = viewModel::onChangelogPermanentlyHiddenChanged,
        onTitleChangeConfirmed = viewModel::onToolbarTitleChangeSubmitted,
        onAppOverlayConfigureButtonClicked = viewModel::onAppOverlayConfigureButtonClicked,
        onRecoveryConfirmed = viewModel::onRecoveryConfirmed,
        onRecoveryDiscarded = viewModel::onRecoveryDiscarded,
        onShortcutPlacementConfirmed = viewModel::onShortcutPlacementConfirmed,
        onShortcutTypeSelected = viewModel::onCreationDialogOptionSelected,
        onCurlImportSelected = viewModel::onCurlImportOptionSelected,
        onShortcutCreationHelpButtonClicked = viewModel::onCreationDialogHelpButtonClicked,
        onNetworkRestrictionsWarningHidden = viewModel::onNetworkRestrictionsWarningHidden,
        onUnlockDialogSubmitted = viewModel::onUnlockDialogSubmitted,
        onDismissed = viewModel::onDialogDismissed,
    )
}
