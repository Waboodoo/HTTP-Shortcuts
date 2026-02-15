package ch.rmy.android.http_shortcuts.activities.sync.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import ch.rmy.android.http_shortcuts.R

@Composable
fun PasswordProtection(
    modifier: Modifier = Modifier,
    label: String,
    password: String,
    onPasswordChanged: (String) -> Unit,
) {
    var showPassword by rememberSaveable {
        mutableStateOf(false)
    }
    TextField(
        modifier = modifier,
        label = {
            Text(label)
        },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            keyboardType = KeyboardType.Password,
            autoCorrectEnabled = false,
        ),
        value = password,
        onValueChange = onPasswordChanged,
        visualTransformation = if (!showPassword) {
            remember { PasswordVisualTransformation() }
        } else {
            VisualTransformation.None
        },
        singleLine = true,
        suffix = {
            Icon(
                modifier = Modifier
                    .clickable(
                        role = Role.Button,
                        onClick = {
                            showPassword = !showPassword
                        },
                    )
                    .padding(horizontal = 2.dp),
                painter = if (showPassword) {
                    painterResource(R.drawable.outline_visibility_off_24)
                } else {
                    painterResource(R.drawable.outline_visibility_24)
                },
                contentDescription = if (showPassword) {
                    stringResource(R.string.button_hide_password)
                } else {
                    stringResource(R.string.button_show_password)
                },
            )
        },
    )
}
