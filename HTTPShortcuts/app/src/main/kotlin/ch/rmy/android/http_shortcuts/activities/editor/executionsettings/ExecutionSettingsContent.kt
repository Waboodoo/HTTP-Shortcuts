package ch.rmy.android.http_shortcuts.activities.editor.executionsettings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.utils.localization.DurationLocalizable
import ch.rmy.android.framework.utils.localization.QuantityStringLocalizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.Checkbox
import ch.rmy.android.http_shortcuts.components.HelpText
import ch.rmy.android.http_shortcuts.components.SelectionField
import ch.rmy.android.http_shortcuts.components.SettingsButton
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.components.VerticalSpacer
import ch.rmy.android.http_shortcuts.data.enums.ConfirmationType
import ch.rmy.android.http_shortcuts.extensions.localize
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Composable
fun ExecutionSettingsContent(
    runInBackground: Boolean,
    delay: Duration,
    waitForConnection: Boolean,
    waitForConnectionOptionVisible: Boolean,
    confirmationType: ConfirmationType?,
    directShareOptionVisible: Boolean,
    launcherShortcut: Boolean,
    secondaryLauncherShortcut: Boolean,
    quickSettingsTileShortcut: Boolean,
    excludeFromHistory: Boolean,
    repetitionInterval: Int?,
    canUseBiometrics: Boolean,
    excludeFromFileSharing: Boolean,
    canUseFiles: Boolean,
    usesFiles: Boolean,
    onRunInBackgroundChanged: (Boolean) -> Unit,
    onLauncherShortcutChanged: (Boolean) -> Unit,
    onSecondaryLauncherShortcutChanged: (Boolean) -> Unit,
    onQuickSettingsTileShortcutChanged: (Boolean) -> Unit,
    onExcludeFromHistoryChanged: (Boolean) -> Unit,
    onConfirmationTypeChanged: (ConfirmationType?) -> Unit,
    onWaitForConnectionChanged: (Boolean) -> Unit,
    onDelayButtonClicked: () -> Unit,
    onRepetitionIntervalChanged: (Int?) -> Unit,
    onExcludeFromFileSharingChanged: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
    ) {
        Column(
            modifier = Modifier.padding(Spacing.MEDIUM),
        ) {
            SelectionField(
                title = stringResource(R.string.settings_title_execution_mode),
                selectedKey = runInBackground,
                items = listOf(
                    false to stringResource(R.string.option_execution_mode_run_in_foreground),
                    true to stringResource(R.string.option_execution_mode_run_in_background),
                ),
                onItemSelected = onRunInBackgroundChanged,
            )

            AnimatedVisibility(visible = runInBackground) {
                HelpText(
                    text = stringResource(R.string.instructions_run_in_background),
                    modifier = Modifier.padding(top = Spacing.SMALL),
                )
            }
        }

        HorizontalDivider()

        Checkbox(
            label = stringResource(R.string.label_launcher_shortcut),
            subtitle = if (directShareOptionVisible) {
                stringResource(R.string.subtitle_launcher_shortcut_with_direct_share)
            } else {
                stringResource(R.string.subtitle_launcher_shortcut)
            },
            checked = launcherShortcut,
            onCheckedChange = onLauncherShortcutChanged,
        )

        Checkbox(
            label = stringResource(R.string.label_secondary_launcher_shortcut),
            subtitle = stringResource(R.string.subtitle_secondary_launcher_shortcut),
            checked = secondaryLauncherShortcut,
            onCheckedChange = onSecondaryLauncherShortcutChanged,
        )

        Checkbox(
            label = stringResource(R.string.label_quick_tile_shortcut),
            checked = quickSettingsTileShortcut,
            onCheckedChange = onQuickSettingsTileShortcutChanged,
        )

        if (canUseFiles) {
            Checkbox(
                label = stringResource(R.string.label_shortcut_as_file_share_target),
                subtitle = stringResource(R.string.subtitle_shortcut_as_file_share_target),
                checked = !excludeFromFileSharing,
                enabled = usesFiles,
                onCheckedChange = {
                    onExcludeFromFileSharingChanged(!it)
                },
            )
        }

        HorizontalDivider()

        ConfirmationTypeSelection(
            modifier = Modifier.padding(Spacing.MEDIUM),
            canUseBiometrics = canUseBiometrics,
            confirmationType = confirmationType,
            onConfirmationTypeChanged = onConfirmationTypeChanged,
        )

        if (waitForConnectionOptionVisible) {
            Checkbox(
                label = stringResource(R.string.label_wait_for_connection),
                checked = waitForConnection,
                onCheckedChange = onWaitForConnectionChanged,
            )
        }

        SettingsButton(
            title = stringResource(R.string.label_delay_execution),
            subtitle = DurationLocalizable(delay).localize(),
            onClick = onDelayButtonClicked,
        )

        HorizontalDivider()

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
                    modifier = Modifier.padding(top = Spacing.SMALL),
                )
            }
        }

        HorizontalDivider()

        Checkbox(
            label = stringResource(R.string.label_exclude_from_history),
            checked = excludeFromHistory,
            onCheckedChange = onExcludeFromHistoryChanged,
        )

        HorizontalDivider()

        VerticalSpacer(Spacing.MEDIUM)
    }
}

@Composable
private fun ConfirmationTypeSelection(
    canUseBiometrics: Boolean,
    confirmationType: ConfirmationType?,
    onConfirmationTypeChanged: (ConfirmationType?) -> Unit,
    modifier: Modifier = Modifier,
) {
    SelectionField(
        modifier = modifier,
        title = stringResource(R.string.label_require_execution_confirmation),
        selectedKey = confirmationType,
        items = listOf(
            null to stringResource(R.string.option_confirmation_none),
            ConfirmationType.SIMPLE to stringResource(R.string.option_confirmation_simple),
        )
            .runIf(canUseBiometrics) {
                plus(ConfirmationType.BIOMETRIC to stringResource(R.string.option_confirmation_biometric))
            },
        onItemSelected = onConfirmationTypeChanged,
    )
}

private val REPETITION_TYPES = listOf(null to StringResLocalizable(R.string.label_no_repetition))
    .plus(
        listOf(10, 15, 20, 30)
            .map {
                it to QuantityStringLocalizable(R.plurals.label_repeat_every_x_minutes, it, it)
            },
    )
    .plus(
        listOf(1, 2, 3, 4, 6, 8, 12, 18, 24, 48)
            .map {
                (it * 60) to QuantityStringLocalizable(R.plurals.label_repeat_every_x_hours, it, it)
            },
    )

@Preview
@Composable
private fun ExecutionSettingsContent_Preview() {
    ExecutionSettingsContent(
        runInBackground = false,
        delay = 0.seconds,
        waitForConnection = false,
        waitForConnectionOptionVisible = false,
        confirmationType = null,
        directShareOptionVisible = true,
        launcherShortcut = true,
        secondaryLauncherShortcut = false,
        quickSettingsTileShortcut = false,
        excludeFromHistory = false,
        repetitionInterval = 0,
        canUseBiometrics = false,
        excludeFromFileSharing = false,
        canUseFiles = true,
        usesFiles = false,
        onRunInBackgroundChanged = {},
        onLauncherShortcutChanged = {},
        onSecondaryLauncherShortcutChanged = {},
        onQuickSettingsTileShortcutChanged = {},
        onExcludeFromHistoryChanged = {},
        onConfirmationTypeChanged = {},
        onWaitForConnectionChanged = {},
        onDelayButtonClicked = {},
        onRepetitionIntervalChanged = {},
        onExcludeFromFileSharingChanged = {},
    )
}
