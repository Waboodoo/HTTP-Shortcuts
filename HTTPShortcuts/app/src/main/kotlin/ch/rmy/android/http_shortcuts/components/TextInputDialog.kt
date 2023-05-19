package ch.rmy.android.http_shortcuts.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import ch.rmy.android.http_shortcuts.R

@Composable
fun TextInputDialog(
    title: String?,
    message: String? = null,
    initialValue: String = "",
    allowEmpty: Boolean = true,
    confirmButton: String = stringResource(R.string.dialog_ok),
    keyboardType: KeyboardType = KeyboardType.Text,
    transformValue: (String) -> String = { it },
    dismissButton: @Composable (() -> Unit)? = null,
    singleLine: Boolean = false,
    onDismissRequest: (newValue: String?) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    var value by remember {
        mutableStateOf(initialValue)
    }
    LaunchedEffect(initialValue) {
        value = initialValue
    }
    val confirmButtonEnabled by remember {
        derivedStateOf {
            allowEmpty || value.isNotEmpty()
        }
    }
    AlertDialog(
        onDismissRequest = {
            onDismissRequest(null)
        },
        title = title?.let {
            {
                Text(title)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.SMALL)
            ) {
                if (message != null) {
                    Text(message)
                }
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    value = value,
                    onValueChange = {
                        value = transformValue(it)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = keyboardType,
                        imeAction = if (singleLine) ImeAction.Go else ImeAction.Default,
                    ),
                    keyboardActions = KeyboardActions {
                        if (confirmButtonEnabled) {
                            onDismissRequest(value)
                        }
                    },
                    textStyle = TextStyle(
                        fontSize = FontSize.SMALL,
                        fontFamily = FontFamily.Monospace,
                    ),
                    visualTransformation = if (keyboardType == KeyboardType.Password) {
                        remember { PasswordVisualTransformation() }
                    } else {
                        VisualTransformation.None
                    },
                    singleLine = singleLine,
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = confirmButtonEnabled,
                onClick = {
                    onDismissRequest(value)
                },
            ) {
                Text(confirmButton)
            }
        },
        dismissButton = dismissButton,
    )
}

@Preview
@Composable
private fun TextInputDialog_Preview() {
    TextInputDialog(
        title = "My Dialog",
        message = "My Message",
        confirmButton = "Yeah!",
        onDismissRequest = {},
    )
}
