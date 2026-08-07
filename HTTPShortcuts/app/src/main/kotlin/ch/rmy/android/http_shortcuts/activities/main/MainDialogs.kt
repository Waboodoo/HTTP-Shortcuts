package ch.rmy.android.http_shortcuts.activities.main

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.main.models.RecoveryInfo
import ch.rmy.android.http_shortcuts.components.ChangeLogDialog
import ch.rmy.android.http_shortcuts.components.ChangeTitleDialog
import ch.rmy.android.http_shortcuts.components.ConfirmDialog
import ch.rmy.android.http_shortcuts.components.HideableDialog
import ch.rmy.android.http_shortcuts.components.IconPickerDialog
import ch.rmy.android.http_shortcuts.components.ProgressDialog
import ch.rmy.android.http_shortcuts.components.SelectDialog
import ch.rmy.android.http_shortcuts.components.SelectDialogEntry
import ch.rmy.android.http_shortcuts.components.UnlockAppDialog
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

@Composable
fun MainDialogs(
    dialogState: MainDialogState?,
    onChangelogPermanentlyHiddenChanged: (Boolean) -> Unit,
    onTitleChangeConfirmed: (String) -> Unit,
    onAppOverlayConfigureButtonClicked: () -> Unit,
    onRecoveryConfirmed: () -> Unit,
    onRecoveryDiscarded: () -> Unit,
    onShortcutPlacementConfirmed: (useLegacy: Boolean) -> Unit,
    onShellApkPermissionConfirmed: () -> Unit,
    onNetworkRestrictionsWarningHidden: (Boolean) -> Unit,
    onUnlockDialogSubmitted: (String) -> Unit,
    onEditCategoryClicked: () -> Unit,
    onManageSectionsClicked: () -> Unit,
    onPlaceCategoryOnHomeScreenClicked: () -> Unit,
    onCategoryIconSelected: (ShortcutIcon) -> Unit,
    onMaterialDesignCategoryIconOptionSelected: () -> Unit,
    onCustomCategoryIconOptionSelected: () -> Unit,
    onDismissed: () -> Unit,
) {
    when (dialogState) {
        is MainDialogState.AppOverlayInfo -> {
            ConfirmDialog(
                message = stringResource(R.string.message_plugin_dialog_configure_app_overlay, stringResource(R.string.dialog_configure)),
                confirmButton = stringResource(R.string.dialog_configure),
                onConfirmRequest = onAppOverlayConfigureButtonClicked,
                onDismissRequest = onDismissed,
            )
        }
        is MainDialogState.ChangeLog -> {
            ChangeLogDialog(
                title = stringResource(R.string.changelog_title_whats_new),
                permanentlyHidden = false,
                onPermanentlyHiddenChanged = onChangelogPermanentlyHiddenChanged,
                onDismissRequested = onDismissed,
            )
        }
        is MainDialogState.ChangeTitle -> {
            ChangeTitleDialog(
                initialValue = dialogState.oldTitle,
                onConfirm = onTitleChangeConfirmed,
                onDismissalRequested = onDismissed,
            )
        }
        is MainDialogState.NetworkRestrictionsWarning -> {
            NetworkRestrictionsWarningDialog(
                onNetworkRestrictionsWarningHidden = onNetworkRestrictionsWarningHidden,
                onDismissed = onDismissed,
            )
        }
        is MainDialogState.RecoverShortcut -> {
            RecoverShortcutDialog(
                recoveryInfo = dialogState.recoveryInfo,
                onConfirmed = onRecoveryConfirmed,
                onDiscarded = onRecoveryDiscarded,
                onDismissed = onDismissed,
            )
        }
        is MainDialogState.ShortcutPlacement -> {
            ShortcutPlacementDialog(
                onShortcutPlacementConfirmed = onShortcutPlacementConfirmed,
                onDismissed = onDismissed,
            )
        }
        is MainDialogState.ShellApkUnknownSourcesPermissionRequired -> {
            ConfirmDialog(
                title = stringResource(R.string.action_install_as_app),
                message = stringResource(R.string.message_shell_apk_unknown_sources_permission_required),
                onConfirmRequest = onShellApkPermissionConfirmed,
                onDismissRequest = onDismissed,
            )
        }
        is MainDialogState.Unlock -> {
            UnlockAppDialog(
                tryAgain = dialogState.tryAgain,
                onSubmitted = onUnlockDialogSubmitted,
                onDismissed = onDismissed,
            )
        }
        is MainDialogState.Progress -> {
            ProgressDialog(
                onDismissRequest = {},
            )
        }
        is MainDialogState.CategoryMenu -> {
            CategoryMenuDialog(
                title = dialogState.title,
                placeOnHomeScreenOptionVisible = dialogState.placeOnHomeScreenOptionVisible,
                onEditClicked = onEditCategoryClicked,
                onManageSectionsClicked = onManageSectionsClicked,
                onPlaceOnHomeScreenClicked = onPlaceCategoryOnHomeScreenClicked,
                onDismissRequested = onDismissed,
            )
        }
        is MainDialogState.CategoryIconPicker -> {
            IconPickerDialog(
                currentIcon = dialogState.currentIcon,
                suggestionBase = dialogState.suggestionBase,
                title = stringResource(R.string.title_category_select_icon),
                onMaterialDesignIconOptionSelected = onMaterialDesignCategoryIconOptionSelected,
                onCustomIconOptionSelected = onCustomCategoryIconOptionSelected,
                onIconSelected = onCategoryIconSelected,
                onDismissRequested = onDismissed,
            )
        }
        is MainDialogState.TooManySyncErrors -> {
            TooManySyncErrorsDialog(
                onDismissed = onDismissed,
            )
        }
        null -> Unit
    }
}

@Composable
private fun NetworkRestrictionsWarningDialog(
    onNetworkRestrictionsWarningHidden: (Boolean) -> Unit,
    onDismissed: () -> Unit,
) {
    HideableDialog(
        message = stringResource(R.string.warning_data_saver_battery_saver_enabled),
        onHidden = onNetworkRestrictionsWarningHidden,
        onDismissed = onDismissed,
    )
}

@Composable
private fun RecoverShortcutDialog(
    recoveryInfo: RecoveryInfo,
    onConfirmed: () -> Unit,
    onDiscarded: () -> Unit,
    onDismissed: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissed,
        title = {
            Text(stringResource(R.string.title_unsaved_changes_detected))
        },
        text = {
            Text(
                if (recoveryInfo.shortcutName.isNotEmpty()) {
                    stringResource(R.string.message_unsaved_changes_detected, recoveryInfo.shortcutName)
                } else {
                    stringResource(R.string.message_unsaved_changes_detected_no_name)
                },
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirmed) {
                Text(stringResource(R.string.button_recover))
            }
        },
        dismissButton = {
            TextButton(onClick = onDiscarded) {
                Text(stringResource(R.string.dialog_discard))
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
    )
}

@Composable
private fun ShortcutPlacementDialog(
    onShortcutPlacementConfirmed: (useLegacy: Boolean) -> Unit,
    onDismissed: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissed,
        title = {
            Text(stringResource(R.string.title_select_placement_method))
        },
        text = {
            Text(stringResource(R.string.description_select_placement_method))
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onShortcutPlacementConfirmed(false)
                },
            ) {
                Text(stringResource(R.string.label_placement_method_default))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onShortcutPlacementConfirmed(true)
                },
            ) {
                Text(stringResource(R.string.label_placement_method_legacy))
            }
        },
    )
}

@Composable
private fun CategoryMenuDialog(
    title: String,
    placeOnHomeScreenOptionVisible: Boolean,
    onEditClicked: () -> Unit,
    onManageSectionsClicked: () -> Unit,
    onPlaceOnHomeScreenClicked: () -> Unit,
    onDismissRequested: () -> Unit,
) {
    SelectDialog(
        title = title,
        onDismissRequest = onDismissRequested,
    ) { horizontalPadding ->
        SelectDialogEntry(
            horizontalPadding = horizontalPadding,
            label = stringResource(R.string.action_edit),
            icon = painterResource(R.drawable.outline_edit_24),
            onClick = onEditClicked,
        )
        SelectDialogEntry(
            horizontalPadding = horizontalPadding,
            label = stringResource(R.string.action_manage_sections),
            icon = painterResource(R.drawable.outline_menu_24),
            onClick = onManageSectionsClicked,
        )
        if (placeOnHomeScreenOptionVisible) {
            SelectDialogEntry(
                horizontalPadding = horizontalPadding,
                label = stringResource(R.string.action_place_category),
                icon = painterResource(R.drawable.outline_add_to_home_screen),
                onClick = onPlaceOnHomeScreenClicked,
            )
        }
    }
}

@Composable
private fun TooManySyncErrorsDialog(
    onDismissed: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissed,
        title = {
            Text(stringResource(R.string.warning_dialog_title))
        },
        text = {
            Text(stringResource(R.string.description_sync_failed_too_many_times))
        },
        confirmButton = {
            TextButton(
                onClick = onDismissed,
            ) {
                Text(stringResource(R.string.dialog_ok))
            }
        },
    )
}
