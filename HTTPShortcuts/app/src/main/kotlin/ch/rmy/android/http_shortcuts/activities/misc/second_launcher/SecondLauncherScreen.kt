package ch.rmy.android.http_shortcuts.activities.misc.second_launcher

import androidx.compose.runtime.Composable
import ch.rmy.android.http_shortcuts.components.ScreenScope
import ch.rmy.android.http_shortcuts.components.bindViewModel

@Composable
fun ScreenScope.SecondLauncherScreen() {
    val (viewModel, state) = bindViewModel<SecondLauncherViewState, SecondLauncherViewModel>()

    SecondLauncherDialogs(
        dialogState = state?.dialogState,
        onShortcutSelected = viewModel::onShortcutSelected,
        onDismissed = viewModel::onDialogDismissed,
    )
}
