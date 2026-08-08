package ch.rmy.android.http_shortcuts.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import ch.rmy.android.http_shortcuts.R

@Composable
fun ChangeTitleDialog(
    initialValue: String,
    onConfirm: (String) -> Unit,
    onDismissalRequested: () -> Unit,
) {
    TextInputDialog(
        title = stringResource(R.string.title_set_title),
        initialValue = initialValue,
        transformValue = { text ->
            text.filter { it != '\n' }
                .take(50)
        },
        imeAction = ImeAction.Go,
        onDismissRequest = { text ->
            if (text != null) {
                onConfirm(text)
            } else {
                onDismissalRequested()
            }
        },
    )
}
