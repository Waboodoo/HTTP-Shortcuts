package ch.rmy.android.http_shortcuts.activities.main

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogs
import ch.rmy.android.http_shortcuts.activities.main.models.CategoryItem
import ch.rmy.android.http_shortcuts.components.EventHandler
import ch.rmy.android.http_shortcuts.components.bindViewModel
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.dtos.ShortcutPlaceholder
import ch.rmy.android.http_shortcuts.data.enums.CategoryBackgroundType
import ch.rmy.android.http_shortcuts.data.enums.SelectionMode
import ch.rmy.android.http_shortcuts.import_export.OpenFilePickerForExportContract

@Composable
fun ShortcutListContent(
    category: CategoryItem,
    selectionMode: SelectionMode,
    isActive: Boolean,
    onPlaceShortcutOnHomeScreen: (ShortcutPlaceholder) -> Unit,
    onRemoveShortcutFromHomeScreen: (ShortcutPlaceholder) -> Unit,
    onSelectShortcut: (ShortcutId) -> Unit,
) {
    val (viewModel, state) = bindViewModel<ShortcutListViewModel.InitData, ShortcutListViewState, ShortcutListViewModel>(
        ShortcutListViewModel.InitData(
            categoryId = category.categoryId,
            selectionMode = selectionMode,
        ),
        key = category.categoryId,
    )

    val openFilePickerForExport = rememberLauncherForActivityResult(OpenFilePickerForExportContract) { fileUri ->
        fileUri?.let(viewModel::onFilePickedForExport)
    }

    if (state == null) {
        return
    }

    EventHandler(enabled = isActive) { event ->
        when (event) {
            is ShortcutListEvent.OpenFilePickerForExport -> consume {
                openFilePickerForExport.launch(
                    OpenFilePickerForExportContract.Params(single = true)
                )
            }
            is ShortcutListEvent.PlaceShortcutOnHomeScreen -> consume {
                onPlaceShortcutOnHomeScreen(event.shortcut)
            }
            is ShortcutListEvent.RemoveShortcutFromHomeScreen -> consume {
                onRemoveShortcutFromHomeScreen(event.shortcut)
            }
            is ShortcutListEvent.SelectShortcut -> consume {
                onSelectShortcut(event.shortcutId)
            }
            else -> false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .runIfNotNull(category.background as? CategoryBackgroundType.Color) {
                background(Color(it.color))
            },
    ) {
        ShortcutList(
            shortcuts = state.shortcuts,
            layoutType = category.layoutType,
            textColor = category.background.textColor(),
            useTextShadows = category.background.useTextShadow,
            isLongClickingEnabled = state.isLongClickingEnabled,
            onShortcutClicked = viewModel::onShortcutClicked,
            onShortcutLongClicked = viewModel::onShortcutLongClicked,
        )
    }

    ShortcutListDialogs(
        dialogState = state.dialogState,
        onPlaceOnHomeScreenOptionSelected = viewModel::onPlaceOnHomeScreenOptionSelected,
        onExecuteOptionSelected = viewModel::onExecuteOptionSelected,
        onCancelPendingExecutionOptionSelected = viewModel::onCancelPendingExecutionOptionSelected,
        onEditOptionSelected = viewModel::onEditOptionSelected,
        onMoveOptionSelected = viewModel::onMoveOptionSelected,
        onDuplicateOptionSelected = viewModel::onDuplicateOptionSelected,
        onDeleteOptionSelected = viewModel::onDeleteOptionSelected,
        onShowInfoOptionSelected = viewModel::onShowInfoOptionSelected,
        onExportOptionSelected = viewModel::onExportOptionSelected,
        onExportToFileOptionSelected = viewModel::onExportToFileOptionSelected,
        onExportViaSharingOptionSelected = viewModel::onExportViaSharingOptionSelected,
        onExportAsCurlOptionSelected = viewModel::onExportAsCurlOptionSelected,
        onExportAsFileOptionSelected = viewModel::onExportAsFileOptionSelected,
        onDeletionConfirmed = viewModel::onDeletionConfirmed,
        onCurlExportCopyButtonClicked = viewModel::onCurlExportCopyButtonClicked,
        onCurlExportShareButtonClicked = viewModel::onCurlExportShareButtonClicked,
        onDismissed = viewModel::onDialogDismissed,
    )

    val executeDialogState by viewModel.executeDialogState.collectAsStateWithLifecycle()
    ExecuteDialogs(
        executeDialogState,
        onResult = viewModel::onExecuteDialogResult,
        onDismissed = viewModel::onExecuteDialogDismissed,
    )
}

@Stable
private fun CategoryBackgroundType.textColor() =
    when (this) {
        is CategoryBackgroundType.Color -> {
            if (Color(color).luminance() < 0.5f) Color.White else Color.Black
        }
        is CategoryBackgroundType.Default -> null
    }
