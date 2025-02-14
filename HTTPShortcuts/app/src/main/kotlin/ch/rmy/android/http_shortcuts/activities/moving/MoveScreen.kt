package ch.rmy.android.http_shortcuts.activities.moving

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.bindViewModel
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId

@Composable
fun MoveScreen(initialShortcut: ShortcutId) {
    val (viewModel, state) = bindViewModel<Unit, MoveViewModel>()
    val categorySections by viewModel.categorySections.collectAsStateWithLifecycle()

    BackHandler(state != null) {
        viewModel.onBackPressed()
    }

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.title_move_shortcuts),
    ) {
        MoveContent(
            categorySections = categorySections,
            initialShortcut = initialShortcut,
            onShortcutMovedToShortcut = viewModel::onShortcutMovedToShortcut,
            onShortcutMovedToCategory = viewModel::onShortcutMovedToCategory,
            onMoveEnded = viewModel::onMoveEnded,
        )
    }
}
