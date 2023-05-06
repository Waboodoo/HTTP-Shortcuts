package ch.rmy.android.http_shortcuts.activities.editor.executionsettings

import androidx.compose.runtime.Stable
import kotlin.time.Duration

@Stable
data class ExecutionSettingsViewState(
    val dialogState: ExecutionSettingsDialogState? = null,
    val delay: Duration,
    val waitForConnection: Boolean,
    val waitForConnectionOptionVisible: Boolean,
    val requireConfirmation: Boolean,
    val launcherShortcutOptionVisible: Boolean,
    val launcherShortcut: Boolean,
    val secondaryLauncherShortcut: Boolean,
    val quickSettingsTileShortcutOptionVisible: Boolean,
    val quickSettingsTileShortcut: Boolean,
    val excludeFromHistory: Boolean,
    val repetitionInterval: Int?,
)
