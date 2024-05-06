package ch.rmy.android.http_shortcuts.activities.main

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.activities.main.models.RecoveryInfo

@Stable
sealed class MainDialogState {

    @Stable
    data object ChangeLog : MainDialogState()

    @Stable
    data object NetworkRestrictionsWarning : MainDialogState()

    @Stable
    data class ChangeTitle(
        val oldTitle: String,
    ) : MainDialogState()

    @Stable
    data object ShortcutPlacement : MainDialogState()

    @Stable
    data class Unlock(
        val tryAgain: Boolean = false,
    ) : MainDialogState()

    @Stable
    data class RecoverShortcut(
        val recoveryInfo: RecoveryInfo,
    ) : MainDialogState()

    @Stable
    data object AppOverlayInfo : MainDialogState()

    @Stable
    data object Progress : MainDialogState()
}
