package ch.rmy.android.http_shortcuts.activities.troubleshooting

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.SettingsButton
import ch.rmy.android.http_shortcuts.components.SettingsSelection
import ch.rmy.android.http_shortcuts.components.Spacing

@Composable
fun TroubleShootingContent(
    batteryOptimizationButtonVisible: Boolean,
    allowXiaomiOverlayButtonVisible: Boolean,
    performanceOptimizationsEnabled: Boolean,
    onEventHistoryClicked: () -> Unit,
    onClearCookiesButtonClicked: () -> Unit,
    onCancelAllPendingExecutionsButtonClicked: () -> Unit,
    onAllowOverlayButtonClicked: () -> Unit,
    onAllowXiaomiOverlayButtonClicked: () -> Unit,
    onBatteryOptimizationButtonClicked: () -> Unit,
    onPerformanceOptimizationsChanged: (Boolean) -> Unit,
    onDocumentationButtonClicked: () -> Unit,
    onContactButtonClicked: () -> Unit,
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = Spacing.SMALL),
    ) {
        SettingsButton(
            icon = painterResource(R.drawable.outline_history_24),
            title = stringResource(R.string.title_event_history),
            onClick = onEventHistoryClicked,
        )

        SettingsButton(
            icon = painterResource(R.drawable.outline_cookie_24),
            title = stringResource(R.string.settings_clear_cookies),
            onClick = onClearCookiesButtonClicked,
        )

        SettingsButton(
            icon = painterResource(R.drawable.outline_schedule_24),
            title = stringResource(R.string.settings_cancel_all_pending_executions),
            onClick = onCancelAllPendingExecutionsButtonClicked,
        )

        SettingsButton(
            icon = painterResource(R.drawable.outline_layers_24),
            title = stringResource(R.string.settings_allow_overlay),
            subtitle = stringResource(R.string.settings_allow_overlay_summary),
            onClick = onAllowOverlayButtonClicked,
        )

        if (allowXiaomiOverlayButtonVisible) {
            SettingsButton(
                icon = painterResource(R.drawable.outline_layers_24),
                title = stringResource(R.string.settings_allow_overlay_xiaomi),
                subtitle = stringResource(R.string.settings_allow_overlay_xiaomi_summary),
                onClick = onAllowXiaomiOverlayButtonClicked,
            )
        }

        if (batteryOptimizationButtonVisible) {
            SettingsButton(
                icon = painterResource(R.drawable.outline_battery_android_frame_full_24),
                title = stringResource(R.string.settings_ignore_battery_optimizations),
                subtitle = stringResource(R.string.settings_ignore_battery_optimizations_summary),
                onClick = onBatteryOptimizationButtonClicked,
            )
        }

        SettingsSelection(
            icon = painterResource(R.drawable.outline_speed_24),
            title = stringResource(R.string.settings_performance_optimizations),
            selectedKey = performanceOptimizationsEnabled,
            items = listOf(
                true to stringResource(R.string.settings_performance_optimizations_enabled),
                false to stringResource(R.string.settings_performance_optimizations_disabled),
            ),
            onItemSelected = onPerformanceOptimizationsChanged,
        )

        HorizontalDivider()

        SettingsButton(
            icon = painterResource(R.drawable.outline_live_help_24),
            title = stringResource(R.string.settings_documentation),
            subtitle = stringResource(R.string.settings_documentation_summary),
            onClick = onDocumentationButtonClicked,
        )

        SettingsButton(
            icon = painterResource(R.drawable.outline_mail_24),
            title = stringResource(R.string.settings_mail),
            subtitle = stringResource(R.string.settings_mail_summary),
            onClick = onContactButtonClicked,
        )
    }
}
