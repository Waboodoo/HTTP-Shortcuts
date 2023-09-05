package ch.rmy.android.http_shortcuts.activities.contact

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.FontSize
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.components.ToolbarIcon
import ch.rmy.android.http_shortcuts.components.bindViewModel

private const val CAPTCHA_CODE = "HTTP Shortcuts"

@Composable
fun ContactScreen() {
    val (viewModel, _) = bindViewModel<Unit, ContactViewModel>()

    var captchaInput by remember {
        mutableStateOf("")
    }
    val isSubmitButtonEnabled by remember {
        derivedStateOf {
            captchaInput.equals(CAPTCHA_CODE, ignoreCase = true)
        }
    }

    SimpleScaffold(
        viewState = Unit,
        title = stringResource(R.string.title_contact),
        actions = {
            ToolbarIcon(
                Icons.Filled.Check,
                contentDescription = stringResource(R.string.dialog_ok),
                enabled = isSubmitButtonEnabled,
                onClick = viewModel::onSubmit,
            )
        },
    ) {
        Column(
            modifier = Modifier
                .padding(Spacing.MEDIUM)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.MEDIUM),
        ) {
            Text(
                text = stringResource(R.string.contact_instructions, CAPTCHA_CODE),
                fontSize = FontSize.MEDIUM,
            )

            TextField(
                modifier = Modifier
                    .fillMaxWidth(),
                value = captchaInput,
                onValueChange = {
                    captchaInput = it
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Go,
                ),
                keyboardActions = KeyboardActions {
                    if (isSubmitButtonEnabled) {
                        viewModel.onSubmit()
                    }
                },
                textStyle = TextStyle(
                    fontSize = FontSize.HUGE,
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Monospace,
                ),
                singleLine = true,
            )
        }
    }
}
