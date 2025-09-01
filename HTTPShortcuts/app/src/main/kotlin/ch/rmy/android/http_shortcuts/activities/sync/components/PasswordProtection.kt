package ch.rmy.android.http_shortcuts.activities.sync.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import ch.rmy.android.http_shortcuts.R

@Composable
fun PasswordProtection(
    modifier: Modifier,
    password: String,
    onPasswordChanged: (String) -> Unit,
) {
    TextField(
        modifier = modifier,
        label = {
            Text(stringResource(R.string.label_password))
        },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            keyboardType = KeyboardType.Password,
            autoCorrectEnabled = false,
        ),
        value = password,
        onValueChange = onPasswordChanged,
        maxLines = 2,
    )
}
