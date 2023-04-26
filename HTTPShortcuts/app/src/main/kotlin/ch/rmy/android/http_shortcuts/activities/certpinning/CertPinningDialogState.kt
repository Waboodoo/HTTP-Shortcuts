package ch.rmy.android.http_shortcuts.activities.certpinning

import androidx.compose.runtime.Stable

@Stable
sealed class CertPinningDialogState {
    @Stable
    object ContextMenu : CertPinningDialogState()

    @Stable
    object ConfirmDeletion : CertPinningDialogState()

    @Stable
    data class Editor(
        val initialHash: String,
        val initialPattern: String,
    ) : CertPinningDialogState()
}
