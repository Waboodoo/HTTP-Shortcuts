package ch.rmy.android.http_shortcuts.activities.misc.quick_settings_tile

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.dtos.ShortcutPlaceholder

@Stable
sealed class QuickSettingsTileDialogState {
    @Stable
    data object Instructions : QuickSettingsTileDialogState()

    @Stable
    data class PickShortcut(
        val shortcuts: List<ShortcutPlaceholder>,
    ) : QuickSettingsTileDialogState()
}
