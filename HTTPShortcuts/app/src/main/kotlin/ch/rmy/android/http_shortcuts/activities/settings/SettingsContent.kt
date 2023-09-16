package ch.rmy.android.http_shortcuts.activities.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.BatteryFull
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.ControlPoint
import androidx.compose.material.icons.outlined.Cookie
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Javascript
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.SettingsButton
import ch.rmy.android.http_shortcuts.components.SettingsGroup
import ch.rmy.android.http_shortcuts.components.SettingsSelection
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.data.enums.ShortcutClickBehavior
import ch.rmy.android.http_shortcuts.utils.Settings.Companion.DARK_THEME_AUTO
import ch.rmy.android.http_shortcuts.utils.Settings.Companion.DARK_THEME_OFF
import ch.rmy.android.http_shortcuts.utils.Settings.Companion.DARK_THEME_ON

@Composable
fun SettingsContent(
    privacySectionVisible: Boolean,
    quickSettingsTileButtonVisible: Boolean,
    batteryOptimizationButtonVisible: Boolean,
    allowOverlayButtonVisible: Boolean,
    allowXiaomiOverlayButtonVisible: Boolean,
    selectedLanguage: String?,
    selectedDarkModeOption: String,
    crashReportingEnabled: Boolean,
    experimentalExecutionModeEnabled: Boolean,
    selectedClickActionOption: ShortcutClickBehavior,
    onLanguageSelected: (String?) -> Unit,
    onDarkModeOptionSelected: (String) -> Unit,
    onClickActionOptionSelected: (ShortcutClickBehavior) -> Unit,
    onChangeTitleButtonClicked: () -> Unit,
    onUserAgentButtonClicked: () -> Unit,
    onLockButtonClicked: () -> Unit,
    onQuickSettingsTileButtonClicked: () -> Unit,
    onCertificatePinningButtonClicked: () -> Unit,
    onGlobalScriptingButtonClicked: () -> Unit,
    onCrashReportingChanged: (Boolean) -> Unit,
    onEventHistoryClicked: () -> Unit,
    onClearCookiesButtonClicked: () -> Unit,
    onCancelAllPendingExecutionsButtonClicked: () -> Unit,
    onAllowOverlayButtonClicked: () -> Unit,
    onAllowXiaomiOverlayButtonClicked: () -> Unit,
    onBatteryOptimizationButtonClicked: () -> Unit,
    onExperimentalExecutionModeChanged: (Boolean) -> Unit,
    onExperimentalHelpTextClicked: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = Spacing.SMALL),
    ) {
        SettingsGroup(
            title = stringResource(R.string.settings_title_basic),
        ) {
            SettingsSelection(
                icon = Icons.Outlined.Language,
                title = stringResource(R.string.settings_language),
                selectedKey = selectedLanguage,
                items = listOf(
                    null to stringResource(R.string.language_default),
                    "en" to stringResource(R.string.language_english),
                    "ca" to stringResource(R.string.language_catalan),
                    "cs" to stringResource(R.string.language_czech),
                    "de" to stringResource(R.string.language_german),
                    "es" to stringResource(R.string.language_spanish),
                    "es-rMX" to stringResource(R.string.language_mexican_spanish),
                    "fr" to stringResource(R.string.language_french),
                    "in" to stringResource(R.string.language_indonesian),
                    "it" to stringResource(R.string.language_italian),
                    "iw" to stringResource(R.string.language_hebrew),
                    "hu" to stringResource(R.string.language_hungarian),
                    "nl" to stringResource(R.string.language_dutch),
                    "nb" to stringResource(R.string.language_norwegian_bokmal),
                    "pl" to stringResource(R.string.language_polish),
                    "pt" to stringResource(R.string.language_portuguese),
                    "pt-rBR" to stringResource(R.string.language_brazilian_portuguese),
                    "ru" to stringResource(R.string.language_russian),
                    "vi" to stringResource(R.string.language_vietnamese),
                    "tr" to stringResource(R.string.language_turkish),
                    "zh-rCN" to stringResource(R.string.language_chinese),
                    "ja" to stringResource(R.string.language_japanese),
                    "ko" to stringResource(R.string.language_korean),
                    "ar" to stringResource(R.string.language_arabic),
                    "el" to stringResource(R.string.language_greek),
                    "fa" to stringResource(R.string.language_persian),
                ),
                onItemSelected = onLanguageSelected,
            )

            SettingsSelection(
                icon = Icons.Outlined.DarkMode,
                title = stringResource(R.string.settings_dark_theme),
                selectedKey = selectedDarkModeOption,
                items = listOf(
                    DARK_THEME_AUTO to stringResource(R.string.settings_dark_theme_options_auto),
                    DARK_THEME_ON to stringResource(R.string.settings_dark_theme_options_on),
                    DARK_THEME_OFF to stringResource(R.string.settings_dark_theme_options_off),
                ),
                onItemSelected = onDarkModeOptionSelected,
            )

            SettingsButton(
                icon = Icons.Outlined.Title,
                title = stringResource(R.string.settings_app_title_title),
                subtitle = stringResource(R.string.settings_app_title_summary),
                onClick = onChangeTitleButtonClicked,
            )

            SettingsButton(
                icon = Icons.Outlined.Lock,
                title = stringResource(R.string.settings_lock_app_title),
                subtitle = stringResource(R.string.settings_lock_app_summary),
                onClick = onLockButtonClicked,
            )

            if (quickSettingsTileButtonVisible) {
                SettingsButton(
                    icon = Icons.Outlined.ControlPoint,
                    title = stringResource(R.string.settings_add_quick_settings_tile_title),
                    subtitle = stringResource(R.string.settings_add_quick_settings_tile_summary),
                    onClick = onQuickSettingsTileButtonClicked,
                )
            }
        }

        SettingsGroup(
            title = stringResource(R.string.settings_title_global_shortcut_settings),
        ) {
            SettingsSelection(
                icon = Icons.Outlined.TouchApp,
                title = stringResource(R.string.settings_click_behavior),
                selectedKey = selectedClickActionOption,
                items = listOf(
                    ShortcutClickBehavior.RUN to stringResource(R.string.settings_click_behavior_run),
                    ShortcutClickBehavior.EDIT to stringResource(R.string.settings_click_behavior_edit),
                    ShortcutClickBehavior.MENU to stringResource(R.string.settings_click_behavior_menu),
                ),
                onItemSelected = onClickActionOptionSelected,
            )

            SettingsButton(
                icon = Icons.Outlined.Badge,
                title = stringResource(R.string.settings_user_agent),
                onClick = onUserAgentButtonClicked,
            )

            SettingsButton(
                icon = Icons.Outlined.Javascript,
                title = stringResource(R.string.settings_global_scripting),
                subtitle = stringResource(R.string.settings_global_scripting_summary),
                onClick = onGlobalScriptingButtonClicked,
            )

            SettingsButton(
                icon = Icons.Outlined.Shield,
                title = stringResource(R.string.settings_certificate_pinning),
                onClick = onCertificatePinningButtonClicked,
            )
        }

        if (privacySectionVisible) {
            SettingsGroup(
                title = stringResource(R.string.settings_title_privacy),
            ) {
                SettingsSelection(
                    icon = Icons.Outlined.BugReport,
                    title = stringResource(R.string.settings_crash_reporting),
                    selectedKey = crashReportingEnabled,
                    items = listOf(
                        true to stringResource(R.string.settings_crash_reporting_allow),
                        false to stringResource(R.string.settings_crash_reporting_disallow),
                    ),
                    onItemSelected = onCrashReportingChanged,
                )
            }
        }

        SettingsGroup(
            title = stringResource(R.string.settings_troubleshooting),
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

            if (allowOverlayButtonVisible) {
                SettingsButton(
                    icon = Icons.Outlined.Layers,
                    title = stringResource(R.string.settings_allow_overlay),
                    subtitle = stringResource(R.string.settings_allow_overlay_summary),
                    onClick = onAllowOverlayButtonClicked,
                )
            }

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
        }

        /*
        SettingsGroup(
            title = stringResource(R.string.settings_title_experimental),
        ) {
            HelpText(
                text = stringResource(R.string.instructions_experimental_section),
                modifier = Modifier
                    .padding(
                        vertical = Spacing.TINY,
                        horizontal = Spacing.MEDIUM,
                    )
                    .clickable(onClick = onExperimentalHelpTextClicked),
            )

            SettingsSelection(
                icon = Icons.Outlined.Science,
                title = stringResource(R.string.settings_title_execution_mode),
                selectedKey = experimentalExecutionModeEnabled,
                items = listOf(
                    false to stringResource(R.string.option_execution_mode_foreground),
                    true to stringResource(R.string.option_execution_mode_background),
                ),
                onItemSelected = onExperimentalExecutionModeChanged,
            )
        }
         */
    }
}
