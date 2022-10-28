package ch.rmy.android.http_shortcuts.activities.main.usecases

import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.main.ShortcutListViewModel
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.extensions.createDialogState
import javax.inject.Inject

class GetMoveOptionsDialogUseCase
@Inject
constructor() {

    operator fun invoke(shortcutId: ShortcutId, viewModel: ShortcutListViewModel) =
        createDialogState {
            item(R.string.action_enable_moving) {
                viewModel.onMoveModeOptionSelected()
            }
                .item(R.string.action_move_to_category) {
                    viewModel.onMoveToCategoryOptionSelected(shortcutId)
                }
                .build()
        }
}
