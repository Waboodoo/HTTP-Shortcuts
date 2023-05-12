package ch.rmy.android.http_shortcuts.activities.misc.share

import android.net.Uri
import androidx.compose.runtime.Composable
import ch.rmy.android.http_shortcuts.components.bindViewModel

@Composable
fun ShareScreen(
    text: String?,
    title: String?,
    fileUris: List<Uri>,
) {
    val (viewModel, state) = bindViewModel<ShareViewModel.InitData, ShareViewState, ShareViewModel>(
        ShareViewModel.InitData(
            text = text,
            title = title,
            fileUris = fileUris,
        )
    )

    ShareDialogs(
        dialogState = state?.dialogState,
        onShortcutSelected = viewModel::onShortcutSelected,
        onDismissed = viewModel::onDialogDismissed,
    )
}
