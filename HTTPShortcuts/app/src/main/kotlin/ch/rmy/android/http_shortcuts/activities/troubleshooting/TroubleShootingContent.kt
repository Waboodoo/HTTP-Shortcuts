package ch.rmy.android.http_shortcuts.activities.troubleshooting

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.LiveHelp
import androidx.compose.material.icons.outlined.BatteryFull
import androidx.compose.material.icons.outlined.Cookie
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.SettingsButton
import ch.rmy.android.http_shortcuts.components.Spacing

@Composable
fun TroubleShootingContent(
    batteryOptimizationButtonVisible: Boolean,
    allowXiaomiOverlayButtonVisible: Boolean,
    onEventHistoryClicked: () -> Unit,
    onClearCookiesButtonClicked: () -> Unit,
    onCancelAllPendingExecutionsButtonClicked: () -> Unit,
    onAllowOverlayButtonClicked: () -> Unit,
    onAllowXiaomiOverlayButtonClicked: () -> Unit,
    onBatteryOptimizationButtonClicked: () -> Unit,
    onDocumentationButtonClicked: () -> Unit,
    onContactButtonClicked: () -> Unit,
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = Spacing.SMALL),
    ) {
        SettingsButton(
            icon = Icons.Outlined.History,
            title = stringResource(R.string.title_event_history),
            onClick = onEventHistoryClicked,
        )

        SettingsButton(
            icon = Icons.Outlined.Cookie,
            title = stringResource(R.string.settings_clear_cookies),
            onClick = onClearCookiesButtonClicked,
        )

        SettingsButton(
            icon = Icons.Outlined.Schedule,
            title = stringResource(R.string.settings_cancel_all_pending_executions),
            onClick = onCancelAllPendingExecutionsButtonClicked,
        )

        SettingsButton(
            icon = Icons.Outlined.Layers,
            title = stringResource(R.string.settings_allow_overlay),
            subtitle = stringResource(R.string.settings_allow_overlay_summary),
            onClick = onAllowOverlayButtonClicked,
        )

        if (allowXiaomiOverlayButtonVisible) {
            SettingsButton(
                icon = Icons.Outlined.Layers,
                title = stringResource(R.string.settings_allow_overlay_xiaomi),
                subtitle = stringResource(R.string.settings_allow_overlay_xiaomi_summary),
                onClick = onAllowXiaomiOverlayButtonClicked,
            )
        }

        if (batteryOptimizationButtonVisible) {
            SettingsButton(
                icon = Icons.Outlined.BatteryFull,
                title = stringResource(R.string.settings_ignore_battery_optimizations),
                subtitle = stringResource(R.string.settings_ignore_battery_optimizations_summary),
                onClick = onBatteryOptimizationButtonClicked,
            )
        }

        HorizontalDivider()

        SettingsButton(
            icon = Icons.AutoMirrored.Outlined.LiveHelp,
            title = stringResource(R.string.settings_documentation),
            subtitle = stringResource(R.string.settings_documentation_summary),
            onClick = onDocumentationButtonClicked,
        )

        SettingsButton(
            icon = Icons.Outlined.Email,
            title = stringResource(R.string.settings_mail),
            subtitle = stringResource(R.string.settings_mail_summary),
            onClick = onContactButtonClicked,
        )
    }
}
