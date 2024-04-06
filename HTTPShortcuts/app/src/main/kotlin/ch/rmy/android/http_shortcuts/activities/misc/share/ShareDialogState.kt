package ch.rmy.android.http_shortcuts.activities.misc.share

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.dtos.ShortcutPlaceholder

@Stable
sealed class ShareDialogState {
    @Stable
    data object Progress : ShareDialogState()

    @Stable
    data object Instructions : ShareDialogState()

    @Stable
    data class PickShortcut(
        val shortcuts: List<ShortcutPlaceholder>,
    ) : ShareDialogState()
}
