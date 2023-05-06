package ch.rmy.android.http_shortcuts.activities.editor.executionsettings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ch.rmy.android.framework.utils.localization.DurationLocalizable
import ch.rmy.android.framework.utils.localization.QuantityStringLocalizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.Checkbox
import ch.rmy.android.http_shortcuts.components.HelpText
import ch.rmy.android.http_shortcuts.components.SelectionField
import ch.rmy.android.http_shortcuts.components.SettingsButton
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.extensions.localize
import kotlin.time.Duration

@Composable
fun ExecutionSettingsContent(
    delay: Duration,
    waitForConnection: Boolean,
    waitForConnectionOptionVisible: Boolean,
    requireConfirmation: Boolean,
    launcherShortcutOptionVisible: Boolean,
    launcherShortcut: Boolean,
    secondaryLauncherShortcut: Boolean,
    quickSettingsTileShortcutOptionVisible: Boolean,
    quickSettingsTileShortcut: Boolean,
    excludeFromHistory: Boolean,
    repetitionInterval: Int?,
    onLauncherShortcutChanged: (Boolean) -> Unit,
    onSecondaryLauncherShortcutChanged: (Boolean) -> Unit,
    onQuickSettingsTileShortcutChanged: (Boolean) -> Unit,
    onExcludeFromHistoryChanged: (Boolean) -> Unit,
    onRequireConfirmationChanged: (Boolean) -> Unit,
    onWaitForConnectionChanged: (Boolean) -> Unit,
    onDelayButtonClicked: () -> Unit,
    onRepetitionIntervalChanged: (Int?) -> Unit,
) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
    ) {
        if (launcherShortcutOptionVisible) {
            Checkbox(
                label = stringResource(R.string.label_launcher_shortcut),
                checked = launcherShortcut,
                onCheckedChange = onLauncherShortcutChanged,
            )
        }

        Checkbox(
            label = stringResource(R.string.label_secondary_launcher_shortcut),
            checked = secondaryLauncherShortcut,
            onCheckedChange = onSecondaryLauncherShortcutChanged,
        )

        if (quickSettingsTileShortcutOptionVisible) {
            Checkbox(
                label = stringResource(R.string.label_quick_tile_shortcut),
                checked = quickSettingsTileShortcut,
                onCheckedChange = onQuickSettingsTileShortcutChanged,
            )
        }

        Divider()

        Checkbox(
            label = stringResource(R.string.label_exclude_from_history),
            checked = excludeFromHistory,
            onCheckedChange = onExcludeFromHistoryChanged,
        )

        Checkbox(
            label = stringResource(R.string.label_require_execution_confirmation),
            checked = requireConfirmation,
            onCheckedChange = onRequireConfirmationChanged,
        )

        if (waitForConnectionOptionVisible) {
            Checkbox(
                label = stringResource(R.string.label_wait_for_connection),
                checked = waitForConnection,
                onCheckedChange = onWaitForConnectionChanged,
            )
        }

        Divider()

        SettingsButton(
            title = stringResource(R.string.label_delay_execution),
            subtitle = DurationLocalizable(delay).localize(),
            onClick = onDelayButtonClicked,
        )

        Divider()

        Column(
            modifier = Modifier.padding(Spacing.MEDIUM),
        ) {
            SelectionField(
                title = stringResource(R.string.label_run_repeatedly),
                selectedKey = repetitionInterval,
                items = REPETITION_TYPES.map { (value, label) -> value to label.localize() },
                onItemSelected = onRepetitionIntervalChanged,
            )

            AnimatedVisibility(visible = repetitionInterval != null) {
                HelpText(
                    text = stringResource(R.string.instructions_repetitions),
                    modifier = Modifier.padding(top = Spacing.TINY),
                )
            }
        }

        Divider()
    }
}

private val REPETITION_TYPES = listOf(null to StringResLocalizable(R.string.label_no_repetition))
    .plus(
        listOf(10, 15, 20, 30)
            .map {
                it to QuantityStringLocalizable(R.plurals.label_repeat_every_x_minutes, it, it)
            }
    )
    .plus(
        listOf(1, 2, 3, 4, 6, 8, 12, 18, 24, 48)
            .map {
                (it * 60) to QuantityStringLocalizable(R.plurals.label_repeat_every_x_hours, it, it)
            }
    )
