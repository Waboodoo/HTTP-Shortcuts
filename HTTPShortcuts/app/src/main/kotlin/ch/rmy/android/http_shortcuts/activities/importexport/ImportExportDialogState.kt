package ch.rmy.android.http_shortcuts.activities.importexport

import androidx.compose.runtime.Stable
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.http_shortcuts.components.models.MenuEntry
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId

@Stable
sealed class ImportExportDialogState {
    data class Progress(val text: Localizable) : ImportExportDialogState()

    data class Error(val message: Localizable) : ImportExportDialogState()

    data class ImportFromUrl(val initialValue: String) : ImportExportDialogState()

    data class ShortcutSelectionForExport(
        val entries: List<MenuEntry<ShortcutId>>,
    ) : ImportExportDialogState()

    object SelectExportDestinationDialog : ImportExportDialogState()
}
