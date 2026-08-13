package ch.rmy.android.http_shortcuts.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import ch.rmy.android.http_shortcuts.R

@Composable
fun UnlockAppDialog(
    tryAgain: Boolean,
    onSubmitted: (String) -> Unit,
    onDismissed: () -> Unit,
) {
    TextInputDialog(
        title = stringResource(R.string.dialog_title_unlock_app),
        message = stringResource(if (tryAgain) R.string.dialog_text_unlock_app_retry else R.string.dialog_text_unlock_app),
        confirmButton = stringResource(R.string.button_unlock_app),
        allowEmpty = false,
        imeAction = ImeAction.Go,
        monospace = true,
        singleLine = true,
        onDismissRequest = {
            if (it != null) {
                onSubmitted(it)
            } else {
                onDismissed()
            }
        },
        keyboardType = KeyboardType.Password,
    )
}
