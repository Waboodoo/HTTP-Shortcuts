package ch.rmy.android.http_shortcuts.activities.importexport

import androidx.compose.runtime.Stable
import ch.rmy.android.framework.utils.localization.Localizable

@Stable
sealed class ExportDialogState {
    data class Progress(val text: Localizable) : ExportDialogState()

    data class Error(val message: Localizable) : ExportDialogState()
}
