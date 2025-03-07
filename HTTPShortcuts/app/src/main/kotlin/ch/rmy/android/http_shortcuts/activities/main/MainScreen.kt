package ch.rmy.android.http_shortcuts.activities.main

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.SavedStateHandle
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.FloatingAddButton
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.ToolbarIcon
import ch.rmy.android.http_shortcuts.components.bindViewModel
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.enums.SelectionMode
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination.Categories.RESULT_CATEGORIES_CHANGED
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination.Companion.RESULT_CHANGES_DISCARDED
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination.ImportExport.RESULT_CATEGORIES_CHANGED_FROM_IMPORT
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination.MoveShortcuts.RESULT_SHORTCUTS_MOVED
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination.Settings.RESULT_APP_LOCKED
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination.Widget.RESULT_WIDGET_SETTINGS_CANCELLED
import ch.rmy.android.http_shortcuts.navigation.ResultHandler
import ch.rmy.curlcommand.CurlCommand

@Composable
fun MainScreen(
    savedStateHandle: SavedStateHandle,
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
        ),
    )

    val activity = LocalActivity.current!!
    LaunchedEffect(Unit) {
        if (activity.intent.action == Intent.ACTION_APPLICATION_PREFERENCES) {
            viewModel.onApplicationSettingsRequested()
        }
    }

    BackHandler(state != null) {
        viewModel.onBackButtonPressed()
    }

    ResultHandler(savedStateHandle) { result ->
        when (result) {
            RESULT_CATEGORIES_CHANGED,
            RESULT_CATEGORIES_CHANGED_FROM_IMPORT,
            RESULT_SHORTCUTS_MOVED,
            -> {
                viewModel.onShortcutsOrCategoriesChanged()
            }
            RESULT_APP_LOCKED -> {
                viewModel.onAppLocked()
            }
            is CurlCommand -> {
                viewModel.onCurlCommandSubmitted(result)
            }
            is NavigationDestination.ShortcutEditor.ShortcutCreatedResult -> {
                viewModel.onShortcutCreated(result.shortcutId)
            }
            is NavigationDestination.ShortcutEditor.ShortcutEditedResult -> {
                viewModel.onShortcutEdited()
            }
            RESULT_CHANGES_DISCARDED -> {
                viewModel.onChangesDiscarded()
            }
            is NavigationDestination.Widget.Result -> {
                viewModel.onWidgetSettingsSubmitted(
                    shortcutId = result.shortcutId,
                    showLabel = result.showLabel,
                    showIcon = result.showIcon,
                    labelColor = result.labelColor,
                    iconScale = result.iconScale,
                )
            }
            RESULT_WIDGET_SETTINGS_CANCELLED -> {
                viewModel.onWidgetSettingsCancelled()
            }
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
                    onWorkingDirectoriesClicked = viewModel::onWorkingDirectoriesClicked,
                    onImportExportButtonClicked = viewModel::onImportExportButtonClicked,
                    onTroubleShootingButtonClicked = viewModel::onTroubleShootingButtonClicked,
                    onSettingsButtonClicked = viewModel::onSettingsButtonClicked,
                    onAboutButtonClicked = viewModel::onAboutButtonClicked,
                )
            }
        },
        onTitleClicked = if (state?.isLocked == false) {
            viewModel::onToolbarTitleClicked
        } else {
            null
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = state?.isCreateButtonVisible == true,
                enter = scaleIn(),
                exit = scaleOut(),
            ) {
                FloatingAddButton(
                    onClick = viewModel::onCreateShortcutButtonClicked,
                    contentDescription = stringResource(R.string.accessibility_label_create_shortcut_fab),
                )
            }
        },
    ) { viewState ->
        MainContent(
            categoryItems = viewState.categoryItems,
            hasMultipleCategories = viewState.hasMultipleCategories,
            activeCategoryId = viewState.activeCategoryId,
            selectionMode = viewState.selectionMode,
            onActiveCategoryIdChanged = viewModel::onActiveCategoryChanged,
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
        onNetworkRestrictionsWarningHidden = viewModel::onNetworkRestrictionsWarningHidden,
        onUnlockDialogSubmitted = viewModel::onUnlockDialogSubmitted,
        onDismissed = viewModel::onDialogDismissed,
    )
}
