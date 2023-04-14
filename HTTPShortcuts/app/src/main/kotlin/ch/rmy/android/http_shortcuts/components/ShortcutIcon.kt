package ch.rmy.android.http_shortcuts.components

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.viewinterop.NoOpUpdate
import ch.rmy.android.http_shortcuts.icons.IconView
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ShortcutIcon(
    shortcutIcon: ShortcutIcon,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
) {
    AndroidView(
        modifier = Modifier
            .size(size)
            .then(modifier),
        factory = {
            IconView(it)
        },
        update = {
            it.setIcon(shortcutIcon)
        },
        onReset = NoOpUpdate,
    )
}
