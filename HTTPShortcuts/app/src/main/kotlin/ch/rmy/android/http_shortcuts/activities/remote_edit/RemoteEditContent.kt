package ch.rmy.android.http_shortcuts.activities.remote_edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.FontSize
import ch.rmy.android.http_shortcuts.components.Spacing

@Composable
fun RemoteEditContent(
    viewState: RemoteEditViewState,
    onPasswordChanged: (String) -> Unit,
    onUploadButtonClicked: () -> Unit,
    onDownloadButtonClicked: () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(Spacing.MEDIUM)
            .verticalScroll(rememberScrollState()),
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
}

@Composable
private fun DeviceId(deviceId: String) {
    TextField(
        modifier = Modifier
            .fillMaxWidth(),
        label = {
            Text(stringResource(R.string.label_remote_edit_device_id))
        },
        value = deviceId,
        onValueChange = {},
        textStyle = TextStyle(
            fontSize = FontSize.HUGE,
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.Monospace,
        ),
        singleLine = true,
        readOnly = true,
    )
}

@Composable
private fun Password(password: String, onPasswordChanged: (String) -> Unit) {
    TextField(
        modifier = Modifier
            .fillMaxWidth(),
        label = {
            Text(stringResource(R.string.label_remote_edit_password))
        },
        value = password,
        onValueChange = onPasswordChanged,
        textStyle = TextStyle(
            fontSize = FontSize.HUGE,
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.Monospace,
        ),
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
    )
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
        },
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
