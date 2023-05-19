package ch.rmy.android.http_shortcuts.activities.execute

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.ColorPickerDialog
import ch.rmy.android.http_shortcuts.components.ConfirmDialog
import ch.rmy.android.http_shortcuts.components.MessageDialog
import ch.rmy.android.http_shortcuts.components.MultiSelectDialog
import ch.rmy.android.http_shortcuts.components.SelectDialog
import ch.rmy.android.http_shortcuts.components.SelectDialogEntry
import ch.rmy.android.http_shortcuts.components.TextInputDialog
import ch.rmy.android.http_shortcuts.components.models.MenuEntry
import ch.rmy.android.http_shortcuts.extensions.localize
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun ExecuteDialogs(
    dialogState: ExecuteDialogState<*>?,
    onResult: (Any) -> Unit,
    onDismissed: () -> Unit,
) {
    var actualDialogState by remember {
        mutableStateOf(dialogState)
    }
    LaunchedEffect(dialogState) {
        if (actualDialogState != null) {
            actualDialogState = null
            // If there was already a dialog present, we hide it and delay the new dialog to ensure that the previous
            // dialog is fully removed from the composition and none of its state lingers.
            delay(100.milliseconds)
        } else {
            actualDialogState = dialogState
        }
    }

    when (dialogState) {
        is ExecuteDialogState.GenericMessage -> {
            MessageDialog(
                title = dialogState.title?.localize(),
                message = dialogState.message.localize(),
                onDismissRequest = onDismissed,
            )
        }
        is ExecuteDialogState.GenericConfirm -> {
            ConfirmDialog(
                title = dialogState.title?.localize(),
                message = dialogState.message.localize(),
                confirmButton = dialogState.confirmButton?.localize() ?: stringResource(R.string.dialog_ok),
                onConfirmRequest = {
                    onResult(Unit)
                },
                onDismissRequest = onDismissed,
            )
        }
        is ExecuteDialogState.ColorPicker -> {
            ColorPickerDialog(
                title = dialogState.title?.localize(),
                initialColor = dialogState.initialColor,
                onColorSelected = {
                    onResult(it)
                },
                onDismissRequested = onDismissed,
            )
        }
        is ExecuteDialogState.TextInput -> {
            TextInputDialog(
                title = dialogState.title?.localize(),
                message = dialogState.message?.localize(),
                allowEmpty = dialogState.type != ExecuteDialogState.TextInput.Type.NUMBER,
                singleLine = dialogState.type != ExecuteDialogState.TextInput.Type.MULTILINE_TEXT,
                keyboardType = when (dialogState.type) {
                    ExecuteDialogState.TextInput.Type.TEXT,
                    ExecuteDialogState.TextInput.Type.MULTILINE_TEXT,
                    -> KeyboardType.Text
                    ExecuteDialogState.TextInput.Type.NUMBER -> KeyboardType.Decimal
                    ExecuteDialogState.TextInput.Type.PASSWORD -> KeyboardType.Password
                },
                onDismissRequest = { value ->
                    if (value != null) {
                        onResult(value)
                    } else {
                        onDismissed()
                    }
                },
            )
        }
        is ExecuteDialogState.Selection -> {
            SelectDialog(
                title = dialogState.title?.localize(),
                onDismissRequest = onDismissed,
            ) {
                dialogState.values.forEach { (value, label) ->
                    SelectDialogEntry(
                        label = label,
                        onClick = {
                            onResult(value)
                        },
                    )
                }
            }
        }
        is ExecuteDialogState.MultiSelection -> {
            MultiSelectDialog(
                title = dialogState.title?.localize(),
                entries = dialogState.values.map { (value, label) ->
                    MenuEntry(value, label)
                },
                allowEmpty = true,
                onDismissRequest = { selected ->
                    if (selected != null) {
                        onResult(selected)
                    } else {
                        onDismissed()
                    }
                },
            )
        }
        null -> Unit
    }
}
