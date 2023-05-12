package ch.rmy.android.http_shortcuts.activities.misc.voice

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.MessageDialog
import ch.rmy.android.http_shortcuts.components.bindViewModel

@Composable
fun VoiceScreen(
    shortcutName: String?,
) {
    val (viewModel, state) = bindViewModel<VoiceViewModel.InitData, VoiceViewState, VoiceViewModel>(
        VoiceViewModel.InitData(
            shortcutName = shortcutName,
        )
    )

    if (state?.shortcutNotFound == true) {
        MessageDialog(
            message = stringResource(R.string.error_shortcut_not_found_for_deep_link, shortcutName!!),
            onDismissRequest = viewModel::onDialogDismissed,
        )
    }
}
