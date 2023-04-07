package ch.rmy.android.http_shortcuts.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import ch.rmy.android.http_shortcuts.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextInputDialog(
    title: String,
    initialValue: String = "",
    allowEmpty: Boolean = true,
    dismissButton: @Composable (() -> Unit)? = null,
    onDismissRequest: (newValue: String?) -> Unit,
) {
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
        title = {
            Text(title)
        },
        text = {
            TextField(
                modifier = Modifier
                    .fillMaxWidth(),
                value = value,
                onValueChange = {
                    value = it
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Go,
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
            )
        },
        confirmButton = {
            TextButton(
                enabled = confirmButtonEnabled,
                onClick = {
                    onDismissRequest(value)
                },
            ) {
                Text(stringResource(R.string.dialog_ok))
            }
        },
        dismissButton = dismissButton,
    )
}
