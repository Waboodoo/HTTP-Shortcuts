package ch.rmy.android.http_shortcuts.activities.settings.importexport

import androidx.compose.runtime.Stable

@Stable
data class ImportExportViewState(
    val dialogState: ImportExportDialogState? = null,
    val useLegacyFormat: Boolean,
)
