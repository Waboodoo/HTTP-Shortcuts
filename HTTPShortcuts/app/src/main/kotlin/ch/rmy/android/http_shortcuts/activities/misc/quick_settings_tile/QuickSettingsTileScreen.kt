package ch.rmy.android.http_shortcuts.activities.misc.quick_settings_tile

import androidx.compose.runtime.Composable
import ch.rmy.android.http_shortcuts.components.bindViewModel

@Composable
fun QuickSettingsTileScreen() {
    val (viewModel, state) = bindViewModel<QuickSettingsTileViewState, QuickSettingsTileViewModel>()

    QuickSettingsTileDialogs(
        dialogState = state?.dialogState,
        onShortcutSelected = viewModel::onShortcutSelected,
        onDismissed = viewModel::onDialogDismissed,
    )
}
