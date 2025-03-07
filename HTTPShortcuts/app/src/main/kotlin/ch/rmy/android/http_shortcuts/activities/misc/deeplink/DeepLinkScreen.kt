package ch.rmy.android.http_shortcuts.activities.misc.deeplink

import android.net.Uri
import androidx.compose.runtime.Composable
import ch.rmy.android.http_shortcuts.components.bindViewModel

@Composable
fun DeepLinkScreen(
    url: Uri?,
) {
    val (viewModel, state) = bindViewModel<DeepLinkViewModel.InitData, DeepLinkViewState, DeepLinkViewModel>(
        DeepLinkViewModel.InitData(
            url = url,
        ),
    )

    DeepLinkDialogs(
        dialogState = state?.dialogState,
        onDismissed = viewModel::onDialogDismissed,
    )
}
