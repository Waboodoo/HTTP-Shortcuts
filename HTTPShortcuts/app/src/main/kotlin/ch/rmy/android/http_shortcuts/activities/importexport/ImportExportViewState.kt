package ch.rmy.android.http_shortcuts.activities.importexport

import androidx.compose.runtime.Stable

@Stable
data class ImportExportViewState(
    val dialogState: ImportExportDialogState? = null,
    val exportEnabled: Boolean,
)
