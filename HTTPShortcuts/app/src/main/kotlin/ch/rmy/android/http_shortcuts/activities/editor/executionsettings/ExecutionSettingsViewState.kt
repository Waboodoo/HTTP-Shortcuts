package ch.rmy.android.http_shortcuts.activities.editor.executionsettings

import ch.rmy.android.framework.utils.localization.DurationLocalizable
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

data class ExecutionSettingsViewState(
    val dialogState: DialogState? = null,
    val delay: Duration = 0.milliseconds,
    val waitForConnection: Boolean = false,
    val waitForConnectionOptionVisible: Boolean = false,
    val requireConfirmation: Boolean = false,
    val launcherShortcutOptionVisible: Boolean = false,
    val launcherShortcut: Boolean = false,
    val secondaryLauncherShortcut: Boolean = false,
    val quickSettingsTileShortcutOptionVisible: Boolean = false,
    val quickSettingsTileShortcut: Boolean = false,
    val excludeFromHistory: Boolean = false,
) {
    val delaySubtitle: Localizable
        get() = DurationLocalizable(delay)
}
