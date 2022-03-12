package ch.rmy.android.http_shortcuts.activities.main

import android.net.Uri
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.import_export.ExportFormat

abstract class ShortcutListEvent : ViewModelEvent() {
    data class ShowContextMenu(
        val shortcutId: String,
        val title: String,
        val isPending: Boolean,
        val isMovable: Boolean,
    ) : ViewModelEvent()

    data class ShowMoveOptionsDialog(val shortcutId: String) : ViewModelEvent()

    data class ShowMoveToCategoryDialog(val shortcutId: String, val categoryOptions: List<CategoryOption>) : ViewModelEvent() {
        data class CategoryOption(val categoryId: String, val name: String)
    }

    data class ShowShortcutInfoDialog(val shortcutId: String, val shortcutName: String) : ViewModelEvent()

    data class ShowExportOptionsDialog(val shortcutId: String) : ViewModelEvent()

    data class ShowFileExportDialog(val shortcutId: String, val format: ExportFormat, val variableIds: Collection<String>) : ViewModelEvent()

    data class StartExport(val shortcutId: String, val uri: Uri, val format: ExportFormat, val variableIds: Collection<String>) : ViewModelEvent()

    data class ShowDeleteDialog(val shortcutId: String, val title: Localizable) : ViewModelEvent()
}
