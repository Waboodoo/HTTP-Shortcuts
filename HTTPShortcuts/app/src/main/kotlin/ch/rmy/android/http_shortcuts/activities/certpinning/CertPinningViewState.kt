package ch.rmy.android.http_shortcuts.activities.certpinning

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.activities.certpinning.models.Pin

@Stable
data class CertPinningViewState(
    val dialogState: CertPinningDialogState? = null,
    val pins: List<Pin> = emptyList(),
)
