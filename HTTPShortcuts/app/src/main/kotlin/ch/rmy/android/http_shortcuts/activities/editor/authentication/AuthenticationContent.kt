package ch.rmy.android.http_shortcuts.activities.editor.authentication

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.lifecycle.SavedStateHandle
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.SelectionField
import ch.rmy.android.http_shortcuts.components.SettingsButton
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.components.VariablePlaceholderTextField
import ch.rmy.android.http_shortcuts.components.VerticalSpacer
import ch.rmy.android.http_shortcuts.data.enums.ClientCertParams
import ch.rmy.android.http_shortcuts.data.enums.ShortcutAuthenticationType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType

@Composable
fun AuthenticationContent(
    savedStateHandle: SavedStateHandle,
    shortcutExecutionType: ShortcutExecutionType,
    authenticationType: ShortcutAuthenticationType?,
    username: String,
    password: String,
    token: String,
    clientCertParams: ClientCertParams?,
    isClientCertButtonEnabled: Boolean,
    onAuthenticationTypeChanged: (ShortcutAuthenticationType?) -> Unit,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onTokenChanged: (String) -> Unit,
    onClientCertButtonClicked: () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(vertical = Spacing.MEDIUM)
            .verticalScroll(rememberScrollState()),
    ) {
        if (shortcutExecutionType == ShortcutExecutionType.HTTP) {
            AuthenticationTypeSelection(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.MEDIUM),
                authenticationType = authenticationType,
                onAuthenticationTypeChanged = onAuthenticationTypeChanged,
            )
        }

        AnimatedVisibility(visible = authenticationType?.usesUsernameAndPassword == true || shortcutExecutionType == ShortcutExecutionType.MQTT) {
            Column {
                if (shortcutExecutionType == ShortcutExecutionType.HTTP) {
                    VerticalSpacer(Spacing.SMALL)
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.SMALL),
                ) {
                    UsernameField(
                        savedStateHandle = savedStateHandle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.MEDIUM),
                        username = username,
                        onUsernameChanged = onUsernameChanged,
                    )

                    PasswordField(
                        savedStateHandle = savedStateHandle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.MEDIUM),
                        password = password,
                        onPasswordChanged = onPasswordChanged,
                    )
                }
            }
        }

        AnimatedVisibility(visible = authenticationType?.usesToken == true) {
            Column {
                VerticalSpacer(Spacing.SMALL)
                TokenField(
                    savedStateHandle = savedStateHandle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.MEDIUM),
                    token = token,
                    onTokenChanged = onTokenChanged,
                )
            }
        }

        if (shortcutExecutionType == ShortcutExecutionType.HTTP) {
            VerticalSpacer(Spacing.SMALL)

            Column(
                modifier = Modifier.padding(top = Spacing.MEDIUM),
            ) {
                HorizontalDivider()

                ClientCertButton(
                    clientCertParams = clientCertParams,
                    enabled = isClientCertButtonEnabled,
                    onClientCertButtonClicked = onClientCertButtonClicked,
                )
            }
        }
    }
}

@Composable
private fun AuthenticationTypeSelection(
    modifier: Modifier,
    authenticationType: ShortcutAuthenticationType?,
    onAuthenticationTypeChanged: (ShortcutAuthenticationType?) -> Unit,
) {
    SelectionField(
        modifier = modifier,
        title = stringResource(R.string.label_authentication_method),
        selectedKey = authenticationType,
        items = listOf(
            null to stringResource(R.string.authentication_none),
            ShortcutAuthenticationType.BASIC to stringResource(R.string.authentication_basic),
            ShortcutAuthenticationType.DIGEST to stringResource(R.string.authentication_digest),
            ShortcutAuthenticationType.BEARER to stringResource(R.string.authentication_bearer),
        ),
        onItemSelected = onAuthenticationTypeChanged,
    )
}

@Composable
private fun UsernameField(
    savedStateHandle: SavedStateHandle,
    modifier: Modifier,
    username: String,
    onUsernameChanged: (String) -> Unit,
) {
    VariablePlaceholderTextField(
        savedStateHandle = savedStateHandle,
        key = "username-input",
        modifier = modifier,
        label = {
            Text(stringResource(R.string.label_username))
        },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            autoCorrectEnabled = false,
        ),
        value = username,
        onValueChange = onUsernameChanged,
        maxLines = 2,
    )
}

@Composable
private fun PasswordField(
    savedStateHandle: SavedStateHandle,
    modifier: Modifier,
    password: String,
    onPasswordChanged: (String) -> Unit,
) {
    VariablePlaceholderTextField(
        savedStateHandle = savedStateHandle,
        key = "password-input",
        modifier = modifier,
        label = {
            Text(stringResource(R.string.label_password))
        },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            autoCorrectEnabled = false,
        ),
        value = password,
        onValueChange = onPasswordChanged,
        maxLines = 2,
    )
}

@Composable
private fun TokenField(
    savedStateHandle: SavedStateHandle,
    modifier: Modifier,
    token: String,
    onTokenChanged: (String) -> Unit,
) {
    VariablePlaceholderTextField(
        savedStateHandle = savedStateHandle,
        key = "token-input",
        modifier = modifier,
        label = {
            Text(stringResource(R.string.label_auth_token))
        },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            autoCorrectEnabled = false,
        ),
        value = token,
        onValueChange = onTokenChanged,
        maxLines = 2,
    )
}

@Composable
private fun ClientCertButton(
    clientCertParams: ClientCertParams?,
    enabled: Boolean,
    onClientCertButtonClicked: () -> Unit,
) {
    SettingsButton(
        enabled = enabled,
        title = stringResource(R.string.label_client_cert),
        subtitle = if (enabled) {
            when (clientCertParams) {
                is ClientCertParams.Alias -> stringResource(R.string.label_subtitle_client_cert_in_use, clientCertParams.alias)
                is ClientCertParams.File -> stringResource(R.string.label_subtitle_client_cert_file_in_use)
                else -> stringResource(R.string.label_subtitle_no_client_cert)
            }
        } else {
            stringResource(R.string.label_subtitle_not_applicable_for_http)
        },
        onClick = onClientCertButtonClicked,
    )
}
