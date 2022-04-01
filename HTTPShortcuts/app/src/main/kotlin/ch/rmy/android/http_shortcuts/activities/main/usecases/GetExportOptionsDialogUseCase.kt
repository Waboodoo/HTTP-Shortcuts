package ch.rmy.android.http_shortcuts.activities.main.usecases

import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.main.ShortcutListViewModel
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId

class GetExportOptionsDialogUseCase {

    operator fun invoke(shortcutId: ShortcutId, viewModel: ShortcutListViewModel) =
        DialogState.create {
            title(R.string.title_export_shortcut_as)
                .item(R.string.action_export_as_curl) {
                    viewModel.onExportAsCurlOptionSelected(shortcutId)
                }
                .item(R.string.action_export_as_file) {
                    viewModel.onExportAsFileOptionSelected(shortcutId)
                }
                .build()
        }
}
