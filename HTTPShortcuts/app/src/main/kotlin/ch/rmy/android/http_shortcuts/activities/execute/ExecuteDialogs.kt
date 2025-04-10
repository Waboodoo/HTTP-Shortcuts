package ch.rmy.android.http_shortcuts.activities.execute

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogState.RichTextDisplay.ButtonResult
import ch.rmy.android.http_shortcuts.components.ColorPickerDialog
import ch.rmy.android.http_shortcuts.components.ConfirmDialog
import ch.rmy.android.http_shortcuts.components.FontSize
import ch.rmy.android.http_shortcuts.components.HideableDialog
import ch.rmy.android.http_shortcuts.components.HtmlRichText
import ch.rmy.android.http_shortcuts.components.MessageDialog
import ch.rmy.android.http_shortcuts.components.MultiSelectDialog
import ch.rmy.android.http_shortcuts.components.SelectDialog
import ch.rmy.android.http_shortcuts.components.SelectDialogEntry
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.components.TextInputDialog
import ch.rmy.android.http_shortcuts.components.models.MenuEntry
import ch.rmy.android.http_shortcuts.data.enums.ResponseDisplayAction
import ch.rmy.android.http_shortcuts.extensions.localize
import coil.compose.AsyncImage
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

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
        if (actualDialogState != null && dialogState != null && actualDialogState!!::class == dialogState::class) {
            actualDialogState = null
            // If there was already a dialog of the same type present, we hide it and delay the new dialog to ensure that the previous
            // dialog is fully removed from the composition and none of its state lingers.
            delay(100.milliseconds)
        }
        actualDialogState = dialogState
    }
    actualDialogState?.let {
        ExecuteDialog(dialogState = it, onResult = onResult, onDismissed = onDismissed)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExecuteDialog(
    dialogState: ExecuteDialogState<*>,
    onResult: (Any) -> Unit,
    onDismissed: () -> Unit,
) {
    when (dialogState) {
        is ExecuteDialogState.GenericMessage -> {
            MessageDialog(
                title = dialogState.title?.localize(),
                message = dialogState.message.localize(),
                onDismissRequest = onDismissed,
            )
        }
        is ExecuteDialogState.Warning -> {
            HideableDialog(
                title = dialogState.title?.localize(),
                message = dialogState.message.localize(),
                onHidden = dialogState.onHidden,
                onDismissed = onDismissed,
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
                initialValue = dialogState.initialValue ?: "",
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
                        onResult(
                            value
                                .runIf(dialogState.type == ExecuteDialogState.TextInput.Type.NUMBER) {
                                    sanitizeNumber(this)
                                },
                        )
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
        is ExecuteDialogState.NumberSlider -> {
            NumberSliderDialog(
                title = dialogState.title?.localize(),
                message = dialogState.message?.localize(),
                initialValue = dialogState.initialValue,
                min = dialogState.min,
                max = dialogState.max,
                stepSize = dialogState.stepSize,
                prefix = dialogState.prefix,
                suffix = dialogState.suffix,
                onConfirmed = {
                    onResult(it)
                },
                onDismissed = onDismissed,
            )
        }
        is ExecuteDialogState.DatePicker -> {
            val state = rememberDatePickerState(
                initialSelectedDateMillis = dialogState.initialDate.atStartOfDay(UTC).toInstant().toEpochMilli(),
            )
            DatePickerDialog(
                modifier = Modifier.padding(Spacing.MEDIUM),
                properties = DialogProperties(usePlatformDefaultWidth = false),
                onDismissRequest = onDismissed,
                confirmButton = {
                    val value = state.selectedDateMillis?.let { Instant.ofEpochMilli(it).atZone(UTC).toLocalDate() }
                    TextButton(
                        enabled = value != null,
                        onClick = {
                            onResult(value!!)
                        },
                    ) {
                        Text(stringResource(R.string.dialog_ok))
                    }
                },
            ) {
                DatePicker(
                    state = state,
                    modifier = Modifier.padding(top = if (dialogState.title != null) 0.dp else Spacing.SMALL),
                    title = dialogState.title?.let {
                        {
                            Text(
                                text = it,
                                fontSize = 20.sp,
                                lineHeight = 24.sp,
                                modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp),
                            )
                        }
                    },
                )
            }
        }
        is ExecuteDialogState.TimePicker -> {
            val state = rememberTimePickerState(
                initialHour = dialogState.initialTime.hour,
                initialMinute = dialogState.initialTime.minute,
            )
            AlertDialog(
                modifier = Modifier.padding(Spacing.MEDIUM),
                properties = DialogProperties(usePlatformDefaultWidth = false),
                onDismissRequest = onDismissed,
                confirmButton = {
                    TextButton(
                        onClick = {
                            onResult(LocalTime.of(state.hour, state.minute))
                        },
                    ) {
                        Text(stringResource(R.string.dialog_ok))
                    }
                },
                title = dialogState.title?.let { { Text(it) } },
                text = {
                    TimePicker(state = state)
                },
            )
        }
        is ExecuteDialogState.RichTextDisplay -> {
            RichTextDisplayDialog(
                title = dialogState.title,
                message = dialogState.message,
                buttons = dialogState.buttons?.take(2)?.reversed(),
                onButtonClicked = { button ->
                    onResult(button)
                },
                onDismissed = onDismissed,
            )
        }
        is ExecuteDialogState.ShowResult -> {
            ShowResultDialog(
                title = dialogState.title,
                content = dialogState.content,
                action = dialogState.action,
                monospace = dialogState.monospace,
                fontSize = dialogState.fontSize?.sp ?: TextUnit.Unspecified,
                onActionButtonClicked = {
                    onResult(Unit)
                },
                onDismissed = onDismissed,
            )
        }
    }
}

@Composable
private fun NumberSliderDialog(
    title: String?,
    message: String?,
    initialValue: Float?,
    min: Float,
    max: Float,
    stepSize: Float,
    prefix: String,
    suffix: String,
    onConfirmed: (Float) -> Unit,
    onDismissed: () -> Unit,
) {
    var sliderValue by rememberSaveable(key = "number-picker-value") {
        mutableFloatStateOf(((initialValue ?: min) - min) / (max - min) * 10000f)
    }
    val roundedValue = remember(sliderValue, min, max, stepSize) {
        ((sliderValue / 10000f) * (max - min) / stepSize).roundToInt() * stepSize + min
    }

    AlertDialog(
        modifier = Modifier.padding(Spacing.MEDIUM),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = onDismissed,
        title = title?.let {
            {
                Text(title)
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.SMALL),
            ) {
                if (message != null) {
                    Text(
                        message,
                    )
                }
                Text(
                    prefix + roundedValue.toString().removeSuffix(".0") + suffix,
                    textAlign = TextAlign.Center,
                    fontSize = FontSize.HUGE,
                )
                Slider(
                    modifier = Modifier.widthIn(min = 300.dp),
                    valueRange = 0f..10000f,
                    value = sliderValue,
                    onValueChange = {
                        sliderValue = it
                    },
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmed(roundedValue)
                },
            ) {
                Text(stringResource(R.string.dialog_ok))
            }
        },
    )
}

@Composable
private fun RichTextDisplayDialog(
    title: String?,
    message: String,
    buttons: List<String>?,
    onButtonClicked: (ButtonResult) -> Unit,
    onDismissed: () -> Unit,
) {
    AlertDialog(
        modifier = Modifier.padding(Spacing.MEDIUM),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        title = title?.let {
            {
                Text(title)
            }
        },
        onDismissRequest = onDismissed,
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState()),
            ) {
                SelectionContainer {
                    HtmlRichText(text = message)
                }
            }
        },
        dismissButton = buttons?.getOrNull(1)?.let { label ->
            {
                TextButton(
                    onClick = {
                        onButtonClicked(
                            ButtonResult.BUTTON1,
                        )
                    },
                ) {
                    Text(label)
                }
            }
        },
        confirmButton = {
            val customButtonLabel = buttons?.getOrNull(0)
            TextButton(
                onClick = {
                    onButtonClicked(
                        if (customButtonLabel != null) {
                            if (buttons.size == 2) {
                                ButtonResult.BUTTON2
                            } else {
                                ButtonResult.BUTTON1
                            }
                        } else {
                            ButtonResult.OK
                        },
                    )
                },
            ) {
                Text(customButtonLabel ?: stringResource(R.string.dialog_ok))
            }
        },
    )
}

@Composable
private fun ShowResultDialog(
    title: String,
    content: ExecuteDialogState.ShowResult.Content,
    action: ResponseDisplayAction?,
    monospace: Boolean,
    fontSize: TextUnit,
    onActionButtonClicked: () -> Unit,
    onDismissed: () -> Unit,
) {
    AlertDialog(
        title = {
            Text(title)
        },
        modifier = Modifier.padding(Spacing.MEDIUM),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = onDismissed,
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                when (content) {
                    is ExecuteDialogState.ShowResult.Content.Image -> {
                        AsyncImage(model = content.imageUri, contentDescription = null)
                    }
                    is ExecuteDialogState.ShowResult.Content.Text -> {
                        if (content.allowHtml) {
                            HtmlRichText(text = content.text, monospace = monospace)
                        } else {
                            Text(
                                text = content.text,
                                fontFamily = if (monospace) FontFamily.Monospace else null,
                                fontSize = fontSize,
                                lineHeight = if (fontSize.isUnspecified) TextUnit.Unspecified else fontSize * 1.2f,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismissed,
            ) {
                Text(stringResource(R.string.dialog_ok))
            }
        },
        dismissButton = action?.let {
            {
                TextButton(
                    onClick = onActionButtonClicked,
                ) {
                    Text(
                        when (action) {
                            ResponseDisplayAction.RERUN -> stringResource(R.string.action_rerun_shortcut)
                            ResponseDisplayAction.SHARE -> stringResource(R.string.share_button)
                            ResponseDisplayAction.COPY -> stringResource(R.string.action_copy_response)
                            ResponseDisplayAction.SAVE -> ""
                        },
                    )
                }
            }
        },
    )
}

private val UTC = ZoneId.of("UTC")

private fun sanitizeNumber(input: String) =
    input.trimEnd('.')
        .let {
            when {
                it.startsWith("-.") -> "-0.${it.drop(2)}"
                it.startsWith(".") -> "0$it"
                it.isEmpty() || it == "-" -> "0"
                else -> it
            }
        }
