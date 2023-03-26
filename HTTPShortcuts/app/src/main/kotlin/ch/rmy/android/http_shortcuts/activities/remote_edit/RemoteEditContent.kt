package ch.rmy.android.http_shortcuts.activities.remote_edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.FontSize
import ch.rmy.android.http_shortcuts.components.Label
import ch.rmy.android.http_shortcuts.components.ProgressDialog
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.components.TextContainer
import ch.rmy.android.http_shortcuts.extensions.localize

@Composable
fun RemoteEditContent(
    viewState: RemoteEditViewState,
    onPasswordChanged: (String) -> Unit,
    onUploadButtonClicked: () -> Unit,
    onDownloadButtonClicked: () -> Unit,
    onProgressDialogDismiss: () -> Unit,
    onServerUrlChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier.padding(Spacing.MEDIUM),
        verticalArrangement = Arrangement.spacedBy(Spacing.SMALL),
    ) {
        DeviceId(viewState.deviceId)
        Password(viewState.password, onPasswordChanged)
        UploadButton(
            enabled = viewState.canUpload,
            onClick = onUploadButtonClicked,
        )
        Instructions(viewState.hostAddress)
        DownloadButton(
            enabled = viewState.canDownload,
            onClick = onDownloadButtonClicked,
        )
    }

    Dialog(viewState.dialogState, onProgressDialogDismiss, onServerUrlChange)
}

@Composable
private fun DeviceId(deviceId: String) {
    Column {
        Label(stringResource(R.string.label_remote_edit_device_id))
        TextContainer {
            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                text = deviceId,
                fontSize = FontSize.HUGE,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun Password(password: String, onPasswordChanged: (String) -> Unit) {
    Column {
        Label(stringResource(R.string.label_remote_edit_password))
        TextContainer {
            BasicTextField(
                modifier = Modifier
                    .fillMaxWidth(),
                value = password,
                onValueChange = onPasswordChanged,
                textStyle = TextStyle(
                    fontSize = FontSize.HUGE,
                    textAlign = TextAlign.Center,
                    color = colorResource(R.color.text_color_primary_dark),
                    fontFamily = FontFamily.Monospace,
                ),
                visualTransformation = PasswordVisualTransformation(),
            )
        }
    }
}

@Composable
private fun UploadButton(enabled: Boolean, onClick: () -> Unit) {
    ActionButton(
        onClick = onClick,
        enabled = enabled,
        text = stringResource(R.string.button_remote_edit_upload),
    )
}

@Composable
private fun ActionButton(enabled: Boolean, text: String, onClick: () -> Unit) {
    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        enabled = enabled,
    ) {
        Text(text)
    }
}

@Composable
private fun Instructions(hostAddress: String) {
    val intro = stringResource(R.string.instructions_remote_edit)
    val line1 = stringResource(R.string.instructions_remote_edit_step_1)
    val line2 = stringResource(R.string.instructions_remote_edit_step_2)
    val line3 = buildAnnotatedString {
        val string = stringResource(R.string.instructions_remote_edit_step_3, hostAddress)
        append(string)
        addStyle(SpanStyle(fontWeight = FontWeight.Bold), string.indexOf(hostAddress), string.indexOf(hostAddress) + hostAddress.length)
    }
    val line4 = stringResource(R.string.instructions_remote_edit_step_4)

    Text(
        buildAnnotatedString {
            append(intro)
            append("\n")
            appendOrderedList(line1, line2, line3, line4)
        }
    )
}

private fun AnnotatedString.Builder.appendOrderedList(vararg items: CharSequence) {
    val paragraphStyle = ParagraphStyle(
        textIndent = TextIndent(restLine = 24.sp),
    )
    items.forEachIndexed { index, item ->
        withStyle(style = paragraphStyle) {
            append("${index + 1}.\t\t")
            when (item) {
                is String -> append(item)
                is AnnotatedString -> append(item)
            }
        }
    }
}

@Composable
private fun DownloadButton(enabled: Boolean, onClick: () -> Unit) {
    ActionButton(
        onClick = onClick,
        enabled = enabled,
        text = stringResource(R.string.button_remote_edit_download),
    )
}

@Composable
private fun Dialog(dialogState: RemoteEditDialogState?, onDismissRequest: () -> Unit, onServerUrlChange: (String) -> Unit) {
    when (dialogState) {
        is RemoteEditDialogState.Error -> {
            MessageDialog(dialogState.message.localize(), onDismissRequest)
        }
        is RemoteEditDialogState.Progress -> {
            ProgressDialog(dialogState.text.localize(), onDismissRequest = onDismissRequest)
        }
        is RemoteEditDialogState.EditServerUrl -> {
            EditServerUrlDialog(
                currentServerUrl = dialogState.currentServerAddress,
                onDismissRequest = { newUrl ->
                    if (newUrl != null) {
                        onServerUrlChange(newUrl)
                    } else {
                        onDismissRequest()
                    }
                }
            )
        }
        null -> Unit
    }
}

@Composable
private fun MessageDialog(message: String, onDismissRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        text = {
            Text(message)
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.dialog_ok))
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditServerUrlDialog(currentServerUrl: String, onDismissRequest: (newUrl: String?) -> Unit) {
    var value by remember {
        mutableStateOf(currentServerUrl)
    }
    AlertDialog(
        onDismissRequest = { onDismissRequest(null) },
        title = {
            Text(stringResource(R.string.title_change_remote_server))
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
                    if (value.isNotEmpty()) {
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
                enabled = value.isNotEmpty(),
                onClick = {
                    onDismissRequest(value)
                },
            ) {
                Text(stringResource(R.string.dialog_ok))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest("")
                },
            ) {
                Text(stringResource(R.string.dialog_reset))
            }
        },
    )
}
