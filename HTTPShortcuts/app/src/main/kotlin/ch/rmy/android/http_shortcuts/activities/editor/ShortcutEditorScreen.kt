package ch.rmy.android.http_shortcuts.activities.editor

import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogs
import ch.rmy.android.http_shortcuts.components.BackButton
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.ToolbarIcon
import ch.rmy.android.http_shortcuts.components.bindViewModel
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.extensions.localize
import ch.rmy.android.http_shortcuts.navigation.NavigationArgStore
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import ch.rmy.android.http_shortcuts.navigation.ResultHandler

@Composable
fun ShortcutEditorScreen(
    savedStateHandle: SavedStateHandle,
    categoryId: CategoryId,
    shortcutId: ShortcutId?,
    curlCommandId: NavigationArgStore.ArgStoreId?,
    executionType: ShortcutExecutionType,
    recoveryMode: Boolean,
) {
    val (viewModel, state) = bindViewModel<ShortcutEditorViewModel.InitData, ShortcutEditorViewState, ShortcutEditorViewModel>(
        ShortcutEditorViewModel.InitData(
            categoryId,
            shortcutId,
            curlCommandId,
            executionType,
            recoveryMode,
        ),
    )

    ResultHandler(savedStateHandle) { result ->
        when (result) {
            is NavigationDestination.IconPicker.Result -> {
                viewModel.onShortcutIconChanged(result.icon)
            }
        }
    }

    BackHandler(state != null) {
        viewModel.onBackPressed()
    }

    SimpleScaffold(
        viewState = state,
        title = stringResource(if (shortcutId != null) R.string.edit_shortcut else R.string.create_shortcut),
        subtitle = state?.toolbarSubtitle?.localize(),
        backButton = BackButton.CROSS,
        actions = { viewState ->
            ToolbarIcon(
                Icons.Filled.PlayArrow,
                enabled = viewState.isExecuteButtonEnabled,
                contentDescription = stringResource(R.string.test_button),
                onClick = viewModel::onTestButtonClicked,
            )
            ToolbarIcon(
                Icons.Filled.Check,
                enabled = viewState.isSaveButtonEnabled,
                contentDescription = stringResource(R.string.save_button),
                onClick = viewModel::onSaveButtonClicked,
            )
        },
    ) { viewState ->
        ShortcutEditorContent(
            shortcutName = viewState.shortcutName,
            shortcutDescription = viewState.shortcutDescription,
            shortcutIcon = viewState.shortcutIcon,
            shortcutExecutionType = viewState.shortcutExecutionType,
            basicSettingsSubtitle = viewState.basicSettingsSubtitle.localize(),
            headersSubtitle = viewState.headersSubtitle.localize(),
            requestBodySubtitle = viewState.requestBodySubtitle.localize(),
            mqttMessagesSubtitle = viewState.mqttMessagesSubtitle.localize(),
            authenticationSettingsSubtitle = viewState.authenticationSettingsSubtitle.localize(),
            scriptingSubtitle = viewState.scriptingSubtitle.localize(),
            triggerShortcutsSubtitle = viewState.triggerShortcutsSubtitle.localize(),
            requestBodyButtonEnabled = viewState.requestBodyButtonEnabled,
            iconLoading = viewState.iconLoading,
            onShortcutNameChanged = viewModel::onShortcutNameChanged,
            onShortcutDescriptionChanged = viewModel::onShortcutDescriptionChanged,
            onShortcutIconClicked = viewModel::onIconClicked,
            onBasicRequestButtonClicked = viewModel::onBasicRequestSettingsButtonClicked,
            onHeadersButtonClicked = viewModel::onHeadersButtonClicked,
            onRequestBodyButtonClicked = viewModel::onRequestBodyButtonClicked,
            onMqttMessagesButtonClicked = viewModel::onMqttMessagesButtonClicked,
            onAuthenticationButtonClicked = viewModel::onAuthenticationButtonClicked,
            onResponseHandlingButtonClicked = viewModel::onResponseHandlingButtonClicked,
            onScriptingButtonClicked = viewModel::onScriptingButtonClicked,
            onTriggerShortcutsButtonClicked = viewModel::onTriggerShortcutsButtonClicked,
            onExecutionSettingsButtonClicked = viewModel::onExecutionSettingsButtonClicked,
            onAdvancedSettingsButtonClicked = viewModel::onAdvancedSettingsButtonClicked,
        )
    }

    ShortcutEditorDialogs(
        dialogState = state?.dialogState,
        onDiscardConfirmed = viewModel::onDiscardDialogConfirmed,
        onIconSelected = viewModel::onShortcutIconChanged,
        onCustomIconOptionSelected = viewModel::onCustomIconOptionSelected,
        onFaviconOptionSelected = viewModel::onFetchFaviconOptionSelected,
        onDismiss = viewModel::onDismissDialog,
    )

    val executeDialogState by viewModel.executeDialogState.collectAsStateWithLifecycle()
    ExecuteDialogs(
        executeDialogState,
        onResult = viewModel::onExecuteDialogResult,
        onDismissed = viewModel::onExecuteDialogDismissed,
    )
}
