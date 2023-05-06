package ch.rmy.android.http_shortcuts.activities.editor.advancedsettings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.extensions.localize
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Composable
fun AdvancedSettingsDialogs(
    dialogState: AdvancedSettingsDialogState?,
    onTimeoutConfirmed: (Duration) -> Unit,
    onDismissed: () -> Unit,
) {
    when (dialogState) {
        is AdvancedSettingsDialogState.TimeoutPicker -> {
            TimeoutPickerDialog(
                initialTimeout = dialogState.initialTimeout,
                onConfirmed = onTimeoutConfirmed,
                onDismissed = onDismissed,
            )
        }
        null -> Unit
    }
}

@Composable
private fun TimeoutPickerDialog(
    initialTimeout: Duration,
    onConfirmed: (Duration) -> Unit,
    onDismissed: () -> Unit,
) {
    var value by rememberSaveable(key = "timeout-dialog-value") {
        mutableStateOf(
            (
                TIMEOUT_OPTIONS.indexOfFirst { it >= initialTimeout }
                    .takeUnless { it == -1 }
                    ?: TIMEOUT_OPTIONS.lastIndex
                ).toFloat()
        )
    }
    val timeout = remember(value) {
        TIMEOUT_OPTIONS[value.toInt()]
    }

    AlertDialog(
        onDismissRequest = onDismissed,
        title = {
            Text(stringResource(R.string.label_timeout))
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
                Slider(
                    modifier = Modifier.widthIn(min = 300.dp),
                    valueRange = 0f..(TIMEOUT_OPTIONS.lastIndex.toFloat()),
                    value = value,
                    onValueChange = {
                        value = it
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

private val TIMEOUT_OPTIONS = arrayOf(
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
)
