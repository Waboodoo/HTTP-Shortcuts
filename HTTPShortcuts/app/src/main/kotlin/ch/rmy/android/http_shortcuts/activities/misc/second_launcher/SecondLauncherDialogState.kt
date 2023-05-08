package ch.rmy.android.http_shortcuts.activities.misc.second_launcher

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.dtos.ShortcutPlaceholder

@Stable
sealed class SecondLauncherDialogState {
    @Stable
    data class PickShortcut(
        val shortcuts: List<ShortcutPlaceholder>,
    ) : SecondLauncherDialogState()
}
