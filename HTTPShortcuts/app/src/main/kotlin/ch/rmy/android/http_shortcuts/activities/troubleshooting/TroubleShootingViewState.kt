package ch.rmy.android.http_shortcuts.activities.troubleshooting

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.enums.ShortcutClickBehavior

@Stable
data class TroubleShootingViewState(
    val dialogState: TroubleShootingDialogState? = null,
    val privacySectionVisible: Boolean,
    val quickSettingsTileButtonVisible: Boolean,
    val batteryOptimizationButtonVisible: Boolean,
    val allowXiaomiOverlayButtonVisible: Boolean,
    val selectedLanguage: String?,
    val selectedDarkModeOption: String,
    val selectedClickActionOption: ShortcutClickBehavior,
    val crashReportingAllowed: Boolean,
    val colorTheme: String,
)
