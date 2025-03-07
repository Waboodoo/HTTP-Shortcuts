package ch.rmy.android.http_shortcuts.activities.workingdirectories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.window.DialogProperties
import ch.rmy.android.framework.extensions.tryOrLog
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.ConfirmDialog
import ch.rmy.android.http_shortcuts.components.FontSize
import ch.rmy.android.http_shortcuts.components.SelectDialog
import ch.rmy.android.http_shortcuts.components.SelectDialogEntry
import ch.rmy.android.http_shortcuts.components.Spacing
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

@Composable
fun WorkingDirectoriesDialogs(
    dialogState: WorkingDirectoriesDialogState?,
    onRenameClicked: () -> Unit,
    onRenameConfirmed: (String) -> Unit,
    onMountClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    onDeleteConfirmed: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    when (dialogState) {
        is WorkingDirectoriesDialogState.ContextMenu -> {
            ContextMenuDialog(
                title = dialogState.title,
                onRenameClicked = onRenameClicked,
                onMountClicked = onMountClicked,
                onDeleteClicked = onDeleteClicked,
                onDismissRequested = onDismissRequest,
            )
        }
        is WorkingDirectoriesDialogState.Rename -> {
            RenameDialog(
                initialValue = dialogState.oldName,
                valuesAlreadyInUse = dialogState.existingNames,
                onConfirm = onRenameConfirmed,
                onDismiss = onDismissRequest,
            )
        }
        is WorkingDirectoriesDialogState.Delete -> {
            DeletionConfirmDialog(
                title = dialogState.title,
                onConfirmed = onDeleteConfirmed,
                onDismissRequested = onDismissRequest,
            )
        }
        null -> Unit
    }
}

@Composable
private fun ContextMenuDialog(
    title: String,
    onRenameClicked: () -> Unit,
    onMountClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    onDismissRequested: () -> Unit,
) {
    SelectDialog(
        title = title,
        onDismissRequest = onDismissRequested,
    ) {
        SelectDialogEntry(
            label = stringResource(R.string.action_rename),
            icon = Icons.Filled.DriveFileRenameOutline,
            onClick = onRenameClicked,
        )
        SelectDialogEntry(
            label = stringResource(R.string.button_mount_working_directory),
            icon = Icons.Filled.Link,
            onClick = onMountClicked,
        )
        SelectDialogEntry(
            label = stringResource(R.string.action_delete),
            icon = Icons.Filled.Delete,
            onClick = onDeleteClicked,
        )
    }
}

@Composable
private fun RenameDialog(
    initialValue: String,
    valuesAlreadyInUse: Set<String>,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var value by remember {
        mutableStateOf(
            TextFieldValue(initialValue, selection = TextRange(initialValue.length)),
        )
    }

    LaunchedEffect(initialValue) {
        value = TextFieldValue(initialValue, selection = TextRange(initialValue.length))
    }
    val nameAlreadyInUsed by remember {
        derivedStateOf {
            value.text in valuesAlreadyInUse
        }
    }
    val confirmButtonEnabled by remember {
        derivedStateOf {
            value.text.isNotBlank() && !nameAlreadyInUsed
        }
    }
    AlertDialog(
        modifier = Modifier.padding(Spacing.MEDIUM),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.title_rename_working_directory))
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.SMALL),
            ) {
                val focusRequester = remember { FocusRequester() }
                val keyboard = LocalSoftwareKeyboardController.current
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    value = value,
                    onValueChange = {
                        value = it.copy(text = sanitizeDirectoryName(it.text))
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Go,
                    ),
                    keyboardActions = KeyboardActions {
                        if (confirmButtonEnabled) {
                            onConfirm(value.text)
                        }
                    },
                    textStyle = TextStyle(
                        fontSize = FontSize.SMALL,
                    ),
                    singleLine = true,
                    isError = nameAlreadyInUsed,
                    supportingText = {
                        if (nameAlreadyInUsed) {
                            Text(stringResource(R.string.validation_directory_name_already_exists))
                        }
                    },
                )

                LaunchedEffect(Unit) {
                    tryOrLog {
                        focusRequester.requestFocus()
                        delay(50.milliseconds)
                        keyboard?.show()
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = confirmButtonEnabled,
                onClick = {
                    onConfirm(value.text.trim())
                },
            ) {
                Text(stringResource(R.string.dialog_ok))
            }
        },
    )
}

private fun sanitizeDirectoryName(name: String): String =
    name.filter { it != '/' && it != ':' }

@Composable
private fun DeletionConfirmDialog(
    title: String,
    onConfirmed: () -> Unit,
    onDismissRequested: () -> Unit,
) {
    ConfirmDialog(
        title = title,
        message = stringResource(R.string.confirm_delete_working_directory_message),
        confirmButton = stringResource(R.string.dialog_delete),
        onConfirmRequest = onConfirmed,
        onDismissRequest = onDismissRequested,
    )
}
