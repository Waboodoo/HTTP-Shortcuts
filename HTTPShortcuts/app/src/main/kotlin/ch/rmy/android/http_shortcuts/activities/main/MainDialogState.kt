package ch.rmy.android.http_shortcuts.activities.main

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.activities.main.models.RecoveryInfo
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

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
    data object ShellApkUnknownSourcesPermissionRequired : MainDialogState()

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

    @Stable
    data class CategoryMenu(
        val title: String,
        val placeOnHomeScreenOptionVisible: Boolean,
    ) : MainDialogState()

    @Stable
    data class CategoryIconPicker(
        val currentIcon: ShortcutIcon.BuiltInIcon?,
        val suggestionBase: String?,
    ) : MainDialogState()

    @Stable
    data object TooManySyncErrors : MainDialogState()
}
