package ch.rmy.android.http_shortcuts.activities.main

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.activities.main.models.RecoveryInfo

@Stable
sealed class MainDialogState {

    @Stable
    object ShortcutCreation : MainDialogState()

    @Stable
    object ChangeLog : MainDialogState()

    @Stable
    object NetworkRestrictionsWarning : MainDialogState()

    @Stable
    data class ChangeTitle(
        val oldTitle: String,
    ) : MainDialogState()

    @Stable
    object ShortcutPlacement : MainDialogState()

    @Stable
    data class Unlock(
        val tryAgain: Boolean = false,
    ) : MainDialogState()

    @Stable
    data class RecoverShortcut(
        val recoveryInfo: RecoveryInfo,
    ) : MainDialogState()

    @Stable
    object AppOverlayInfo : MainDialogState()

    @Stable
    object Progress : MainDialogState()
}
