package ch.rmy.android.http_shortcuts.activities.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.DialogProperties
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.main.models.RecoveryInfo
import ch.rmy.android.http_shortcuts.components.ChangeLogDialog
import ch.rmy.android.http_shortcuts.components.ChangeTitleDialog
import ch.rmy.android.http_shortcuts.components.ConfirmDialog
import ch.rmy.android.http_shortcuts.components.FontSize
import ch.rmy.android.http_shortcuts.components.ProgressDialog
import ch.rmy.android.http_shortcuts.components.SelectDialog
import ch.rmy.android.http_shortcuts.components.SelectDialogEntry
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.components.TextInputDialog
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType

@Composable
fun MainDialogs(
    dialogState: MainDialogState?,
    onChangelogPermanentlyHiddenChanged: (Boolean) -> Unit,
    onTitleChangeConfirmed: (String) -> Unit,
    onAppOverlayConfigureButtonClicked: () -> Unit,
    onRecoveryConfirmed: () -> Unit,
    onRecoveryDiscarded: () -> Unit,
    onShortcutPlacementConfirmed: (useLegacy: Boolean) -> Unit,
    onShortcutTypeSelected: (ShortcutExecutionType) -> Unit,
    onCurlImportSelected: () -> Unit,
    onShortcutCreationHelpButtonClicked: () -> Unit,
    onNetworkRestrictionsWarningHidden: (Boolean) -> Unit,
    onUnlockDialogSubmitted: (String) -> Unit,
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
        is MainDialogState.ShortcutCreation -> {
            ShortcutCreationDialog(
                onShortcutTypeSelected = onShortcutTypeSelected,
                onCurlImportSelected = onCurlImportSelected,
                onHelpButtonClicked = onShortcutCreationHelpButtonClicked,
                onDismissed = onDismissed,
            )
        }
        is MainDialogState.ShortcutPlacement -> {
            ShortcutPlacementDialog(
                onShortcutPlacementConfirmed = onShortcutPlacementConfirmed,
                onDismissed = onDismissed,
            )
        }
        is MainDialogState.Unlock -> {
            UnlockDialog(
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
        null -> Unit
    }
}

@Composable
private fun NetworkRestrictionsWarningDialog(
    onNetworkRestrictionsWarningHidden: (Boolean) -> Unit,
    onDismissed: () -> Unit,
) {
    var permanentlyHidden by remember {
        mutableStateOf(false)
    }
    AlertDialog(
        onDismissRequest = onDismissed,
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.SMALL),
            ) {
                Text(stringResource(R.string.warning_data_saver_battery_saver_enabled))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            permanentlyHidden = !permanentlyHidden
                            onNetworkRestrictionsWarningHidden(permanentlyHidden)
                        }
                        .padding(Spacing.TINY),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.SMALL, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = !permanentlyHidden,
                        onCheckedChange = null,
                    )
                    Text(
                        stringResource(R.string.dialog_checkbox_do_not_show_again),
                        fontSize = FontSize.MEDIUM,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissed) {
                Text(stringResource(R.string.dialog_ok))
            }
        },
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
                }
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
private fun ShortcutCreationDialog(
    onShortcutTypeSelected: (ShortcutExecutionType) -> Unit,
    onCurlImportSelected: () -> Unit,
    onHelpButtonClicked: () -> Unit,
    onDismissed: () -> Unit,
) {
    SelectDialog(
        title = stringResource(R.string.title_create_new_shortcut_options_dialog),
        onDismissRequest = onDismissed,
        extraButton = {
            TextButton(onClick = onHelpButtonClicked) {
                Text(stringResource(R.string.dialog_help))
            }
        },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            SelectDialogEntry(
                label = stringResource(R.string.button_create_new),
                onClick = {
                    onShortcutTypeSelected(ShortcutExecutionType.APP)
                },
            )
            SelectDialogEntry(
                label = stringResource(R.string.button_curl_import),
                onClick = onCurlImportSelected,
            )
            Divider(
                modifier = Modifier.padding(vertical = Spacing.MEDIUM),
            )
            SelectDialogEntry(
                label = stringResource(R.string.button_create_trigger_shortcut),
                description = stringResource(R.string.button_description_create_trigger_shortcut),
                onClick = {
                    onShortcutTypeSelected(ShortcutExecutionType.TRIGGER)
                },
            )
            SelectDialogEntry(
                label = stringResource(R.string.button_create_browser_shortcut),
                description = stringResource(R.string.button_description_create_browser_shortcut),
                onClick = {
                    onShortcutTypeSelected(ShortcutExecutionType.BROWSER)
                },
            )
            SelectDialogEntry(
                label = stringResource(R.string.button_create_scripting_shortcut),
                description = stringResource(R.string.button_description_create_scripting_shortcut),
                onClick = {
                    onShortcutTypeSelected(ShortcutExecutionType.SCRIPTING)
                },
            )
        }
    }
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
private fun UnlockDialog(
    tryAgain: Boolean,
    onSubmitted: (String) -> Unit,
    onDismissed: () -> Unit,
) {
    TextInputDialog(
        title = stringResource(R.string.dialog_title_unlock_app),
        message = stringResource(if (tryAgain) R.string.dialog_text_unlock_app_retry else R.string.dialog_text_unlock_app),
        confirmButton = stringResource(R.string.button_unlock_app),
        allowEmpty = false,
        onDismissRequest = {
            if (it != null) {
                onSubmitted(it)
            } else {
                onDismissed()
            }
        },
        keyboardType = KeyboardType.Password,
    )
}
