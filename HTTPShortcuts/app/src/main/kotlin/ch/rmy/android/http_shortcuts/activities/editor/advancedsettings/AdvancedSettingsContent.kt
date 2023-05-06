package ch.rmy.android.http_shortcuts.activities.editor.advancedsettings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.Checkbox
import ch.rmy.android.http_shortcuts.components.SelectionField
import ch.rmy.android.http_shortcuts.components.SettingsButton
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.components.VariablePlaceholderTextField
import ch.rmy.android.http_shortcuts.data.enums.ProxyType

@Composable
fun AdvancedSettingsContent(
    followRedirects: Boolean,
    storeCookies: Boolean,
    requireSpecificWifi: Boolean,
    wifiSsid: String,
    timeoutSubtitle: String,
    proxyType: ProxyType?,
    proxyHost: String,
    proxyPort: String,
    proxyUsername: String,
    proxyPassword: String,
    acceptAllCertificates: Boolean,
    onFollowRedirectsChanged: (Boolean) -> Unit,
    onStoreCookiesChanged: (Boolean) -> Unit,
    onRequireSpecificWifiChanged: (Boolean) -> Unit,
    onWifiSsidChanged: (String) -> Unit,
    onTimeoutButtonClicked: () -> Unit,
    onProxyTypeChanged: (ProxyType?) -> Unit,
    onProxyHostChanged: (String) -> Unit,
    onProxyPortChanged: (String) -> Unit,
    onProxyUsernameChanged: (String) -> Unit,
    onProxyPasswordChanged: (String) -> Unit,
    onAcceptAllCertificates: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
    ) {
        Checkbox(
            label = stringResource(R.string.label_follow_redirects),
            checked = followRedirects,
            onCheckedChange = onFollowRedirectsChanged,
        )

        Checkbox(
            label = stringResource(R.string.label_accept_cookies),
            checked = storeCookies,
            onCheckedChange = onStoreCookiesChanged,
        )

        Divider()

        Column {
            Checkbox(
                label = stringResource(R.string.label_require_specific_wifi_ssid),
                checked = requireSpecificWifi,
                onCheckedChange = onRequireSpecificWifiChanged,
            )
            AnimatedVisibility(visible = requireSpecificWifi) {
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.MEDIUM)
                        .padding(bottom = Spacing.MEDIUM),
                    label = {
                        Text(stringResource(R.string.label_ssid))
                    },
                    supportingText = {
                        Text(stringResource(R.string.message_permission_rational))
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        autoCorrect = false,
                    ),
                    value = wifiSsid,
                    onValueChange = onWifiSsidChanged,
                    singleLine = true,
                )
            }
        }

        Divider()

        SettingsButton(
            title = stringResource(R.string.label_timeout),
            subtitle = timeoutSubtitle,
            onClick = onTimeoutButtonClicked,
        )

        Divider()

        Column(
            modifier = Modifier.padding(Spacing.MEDIUM),
            verticalArrangement = Arrangement.spacedBy(Spacing.SMALL),
        ) {
            SelectionField(
                title = stringResource(R.string.label_proxy_type),
                selectedKey = proxyType,
                items = listOf(
                    null to stringResource(R.string.option_no_proxy),
                    ProxyType.HTTP to "HTTP",
                    ProxyType.SOCKS to "SOCKS",
                ),
                onItemSelected = onProxyTypeChanged,
            )
            AnimatedVisibility(visible = proxyType != null) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.SMALL),
                ) {
                    ProxyHostField(
                        host = proxyHost,
                        onHostChanged = onProxyHostChanged,
                    )

                    ProxyPortField(
                        modifier = Modifier.fillMaxWidth(),
                        port = proxyPort,
                        onPortChanged = onProxyPortChanged,
                    )
                }
            }

            AnimatedVisibility(visible = proxyType?.supportsAuthentication == true) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.SMALL),
                ) {
                    ProxyUsernameField(
                        username = proxyUsername,
                        onUsernameChanged = onProxyUsernameChanged,
                    )

                    ProxyPasswordField(
                        password = proxyPassword,
                        onPasswordChanged = onProxyPasswordChanged,
                    )
                }
            }
        }

        Divider()

        Checkbox(
            label = stringResource(R.string.label_accept_all_certificates),
            subtitle = stringResource(R.string.subtitle_accept_all_certificates),
            checked = acceptAllCertificates,
            onCheckedChange = onAcceptAllCertificates,
        )

        Divider()
    }
}

@Composable
private fun ProxyHostField(
    modifier: Modifier = Modifier,
    host: String,
    onHostChanged: (String) -> Unit,
) {
    VariablePlaceholderTextField(
        key = "proxy-host-input",
        modifier = modifier,
        label = {
            Text(stringResource(R.string.label_proxy_host))
        },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            autoCorrect = false,
        ),
        value = host,
        onValueChange = onHostChanged,
        maxLines = 2,
    )
}

@Composable
private fun ProxyPortField(
    modifier: Modifier = Modifier,
    port: String,
    onPortChanged: (String) -> Unit,
) {
    TextField(
        modifier = modifier,
        label = {
            Text(stringResource(R.string.label_proxy_port))
        },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            keyboardType = KeyboardType.Number,
            autoCorrect = false,
        ),
        value = port,
        onValueChange = { text ->
            onPortChanged(text.filter { it.isDigit() }.take(6))
        },
        singleLine = true,
    )
}

@Composable
private fun ProxyUsernameField(
    modifier: Modifier = Modifier,
    username: String,
    onUsernameChanged: (String) -> Unit,
) {
    VariablePlaceholderTextField(
        key = "proxy-username-input",
        modifier = modifier,
        label = {
            Text(stringResource(R.string.label_proxy_username))
        },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            autoCorrect = false,
        ),
        value = username,
        onValueChange = onUsernameChanged,
        maxLines = 2,
    )
}

@Composable
private fun ProxyPasswordField(
    modifier: Modifier = Modifier,
    password: String,
    onPasswordChanged: (String) -> Unit,
) {
    VariablePlaceholderTextField(
        key = "proxy-password-input",
        modifier = modifier,
        label = {
            Text(stringResource(R.string.label_proxy_password))
        },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            autoCorrect = false,
        ),
        value = password,
        onValueChange = onPasswordChanged,
        maxLines = 2,
    )
}
