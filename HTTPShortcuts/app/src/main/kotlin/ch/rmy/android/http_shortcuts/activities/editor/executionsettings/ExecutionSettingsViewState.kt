package ch.rmy.android.http_shortcuts.activities.editor.executionsettings

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.enums.ConfirmationType
import kotlin.time.Duration

@Stable
data class ExecutionSettingsViewState(
    val dialogState: ExecutionSettingsDialogState? = null,
    val runInBackground: Boolean,
    val delay: Duration,
    val waitForConnection: Boolean,
    val waitForConnectionOptionVisible: Boolean,
    val confirmationType: ConfirmationType?,
    val directShareOptionVisible: Boolean,
    val launcherShortcut: Boolean,
    val secondaryLauncherShortcut: Boolean,
    val quickSettingsTileShortcut: Boolean,
    val excludeFromHistory: Boolean,
    val repetitionInterval: Int?,
    val canUseBiometrics: Boolean,
    val excludeFromFileSharing: Boolean,
    val canUseFiles: Boolean,
    val usesFiles: Boolean,
)
