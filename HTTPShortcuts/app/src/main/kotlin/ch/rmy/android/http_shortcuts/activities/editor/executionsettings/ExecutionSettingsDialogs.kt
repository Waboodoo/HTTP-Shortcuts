package ch.rmy.android.http_shortcuts.activities.editor.executionsettings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.rmy.android.framework.utils.localization.DurationLocalizable
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.ConfirmDialog
import ch.rmy.android.http_shortcuts.components.MessageDialog
import ch.rmy.android.http_shortcuts.components.OrderedOptionsSlider
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.extensions.localize
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Composable
fun ExecutionSettingsDialogs(
    dialogState: ExecutionSettingsDialogState?,
    onConfirmAppOverlay: () -> Unit,
    onConfirmDelay: (Duration) -> Unit,
    onRunInBackgroundInfoDismissed: () -> Unit,
    onDismissed: () -> Unit,
) {
    when (dialogState) {
        is ExecutionSettingsDialogState.AppOverlayPrompt -> {
            ConfirmDialog(
                message = stringResource(R.string.message_run_repeatedly_dialog_configure_app_overlay, stringResource(R.string.dialog_configure)),
                confirmButton = stringResource(R.string.dialog_configure),
                onConfirmRequest = onConfirmAppOverlay,
                onDismissRequest = onDismissed,
            )
        }
        is ExecutionSettingsDialogState.DelayPicker -> {
            DelayPickerDialog(
                initialDelay = dialogState.initialDelay,
                onConfirmed = onConfirmDelay,
                onDismissed = onDismissed,
            )
        }
        is ExecutionSettingsDialogState.RunInBackgroundInfo -> {
            MessageDialog(
                message = stringResource(R.string.dialog_text_run_in_background),
                onDismissRequest = onRunInBackgroundInfoDismissed,
            )
        }
        null -> Unit
    }
}

@Composable
private fun DelayPickerDialog(
    initialDelay: Duration,
    onConfirmed: (Duration) -> Unit,
    onDismissed: () -> Unit,
) {
    var value by rememberSaveable(key = "delay-dialog-value") {
        mutableLongStateOf(initialDelay.inWholeMilliseconds)
    }
    val timeout = remember(value) {
        value.milliseconds
    }

    AlertDialog(
        onDismissRequest = onDismissed,
        title = {
            Text(stringResource(R.string.label_delay_execution))
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.SMALL),
            ) {
                Text(
                    DurationLocalizable(timeout).localize(),
                    textAlign = TextAlign.Center,
                )
                OrderedOptionsSlider(
                    modifier = Modifier.widthIn(min = 300.dp),
                    options = DELAY_OPTIONS,
                    value = timeout,
                    onValueChange = {
                        value = it.inWholeMilliseconds
                    },
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmed(timeout)
                },
            ) {
                Text(stringResource(R.string.dialog_ok))
            }
        },
    )
}

private val DELAY_OPTIONS = arrayOf(
    0.milliseconds,
    500.milliseconds,
    1.seconds,
    2.seconds,
    3.seconds,
    5.seconds,
    8.seconds,
    10.seconds,
    15.seconds,
    20.seconds,
    25.seconds,
    30.seconds,
    45.seconds,
    1.minutes,
    90.seconds,
    2.minutes,
    3.minutes,
    5.minutes,
    450.seconds,
    10.minutes,
    15.minutes,
    20.minutes,
    30.minutes,
    1.hours,
)
