package ch.rmy.android.http_shortcuts.activities.settings

import androidx.compose.runtime.Stable

@Stable
sealed class SettingsDialogState {
    @Stable
    data class ChangeTitle(val oldTitle: String) : SettingsDialogState()

    @Stable
    data class ChangeUserAgent(
        val oldUserAgent: String,
        val placeholder: String,
    ) : SettingsDialogState()

    @Stable
    data class LockApp(
        val canUseBiometrics: Boolean,
        val hasLock: Boolean,
        val usesBiometrics: Boolean,
    ) : SettingsDialogState()

    @Stable
    data object ClearCookies : SettingsDialogState()

    @Stable
    data class Unlock(
        val tryAgain: Boolean = false,
    ) : SettingsDialogState()

    @Stable
    data object Progress : SettingsDialogState()
}
