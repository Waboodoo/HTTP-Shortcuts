package ch.rmy.android.http_shortcuts.activities.moving

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.bindViewModel

@Composable
fun MoveScreen() {
    val (viewModel, state) = bindViewModel<Unit, MoveViewModel>()
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.title_move_shortcuts),
    ) {
        MoveContent(
            categories = categories,
            onShortcutMovedToShortcut = viewModel::onShortcutMovedToShortcut,
            onShortcutMovedToCategory = viewModel::onShortcutMovedToCategory,
            onMoveEnded = viewModel::onMoveEnded,
        )
    }
}
