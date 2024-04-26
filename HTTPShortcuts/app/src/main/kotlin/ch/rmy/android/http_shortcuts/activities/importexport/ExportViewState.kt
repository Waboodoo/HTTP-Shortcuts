package ch.rmy.android.http_shortcuts.activities.importexport

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.activities.importexport.models.ExportItem

@Stable
data class ExportViewState(
    val dialogState: ExportDialogState? = null,
    val items: List<ExportItem>,
) {
    val isExportEnabled: Boolean =
        items.filterIsInstance<ExportItem.Shortcut>().any { it.checked }

    val isSelectAllEnabled: Boolean =
        items.filterIsInstance<ExportItem.Shortcut>().any { !it.checked }
}
