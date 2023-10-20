package ch.rmy.android.http_shortcuts.activities.settings

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.enums.ShortcutClickBehavior

@Stable
data class SettingsViewState(
    val dialogState: SettingsDialogState? = null,
    val privacySectionVisible: Boolean,
    val quickSettingsTileButtonVisible: Boolean,
    val batteryOptimizationButtonVisible: Boolean,
    val allowOverlayButtonVisible: Boolean,
    val allowXiaomiOverlayButtonVisible: Boolean,
    val selectedLanguage: String?,
    val selectedDarkModeOption: String,
    val selectedClickActionOption: ShortcutClickBehavior,
    val crashReportingAllowed: Boolean,
    val colorTheme: String,
    val experimentalExecutionModeEnabled: Boolean,
)
