package ch.rmy.android.http_shortcuts.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import ch.rmy.android.framework.extensions.tryOrLog
import ch.rmy.android.http_shortcuts.R
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

@Composable
fun TextInputDialog(
    title: String?,
    message: String? = null,
    initialValue: String = "",
    allowEmpty: Boolean = true,
    confirmButton: String = stringResource(R.string.dialog_ok),
    keyboardType: KeyboardType = KeyboardType.Text,
    monospace: Boolean = keyboardType == KeyboardType.Password,
    transformValue: (String) -> String = { it },
    dismissButton: @Composable (() -> Unit)? = null,
    singleLine: Boolean = false,
    bottomContent: (@Composable () -> Unit)? = null,
    onDismissRequest: (newValue: String?) -> Unit,
) {
    var value by remember {
        mutableStateOf(
            TextFieldValue(initialValue, selection = TextRange(initialValue.length)),
        )
    }
    LaunchedEffect(initialValue) {
        value = TextFieldValue(initialValue, selection = TextRange(initialValue.length))
    }
    val confirmButtonEnabled by remember {
        derivedStateOf {
            allowEmpty || value.text.isNotEmpty()
        }
    }
    AlertDialog(
        modifier = Modifier.padding(Spacing.MEDIUM),
        properties = DialogProperties(usePlatformDefaultWidth = false),
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
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.SMALL),
            ) {
                if (message != null) {
                    Text(message)
                }
                val focusRequester = remember { FocusRequester() }
                val keyboard = LocalSoftwareKeyboardController.current
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    value = value,
                    onValueChange = {
                        value = it.copy(text = transformValue(it.text))
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = keyboardType,
                        imeAction = if (singleLine) ImeAction.Go else ImeAction.Default,
                    ),
                    keyboardActions = KeyboardActions {
                        if (confirmButtonEnabled) {
                            onDismissRequest(value.text)
                        }
                    },
                    textStyle = TextStyle(
                        fontSize = FontSize.SMALL,
                        fontFamily = if (monospace) FontFamily.Monospace else null,
                    ),
                    visualTransformation = if (keyboardType == KeyboardType.Password) {
                        remember { PasswordVisualTransformation() }
                    } else {
                        VisualTransformation.None
                    },
                    singleLine = singleLine,
                )

                bottomContent?.invoke()

                LaunchedEffect(Unit) {
                    tryOrLog {
                        focusRequester.requestFocus()
                        delay(50.milliseconds)
                        keyboard?.show()
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = confirmButtonEnabled,
                onClick = {
                    onDismissRequest(value.text)
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
