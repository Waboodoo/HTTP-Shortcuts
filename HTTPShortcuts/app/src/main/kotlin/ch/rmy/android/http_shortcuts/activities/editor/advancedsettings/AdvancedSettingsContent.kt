package ch.rmy.android.http_shortcuts.activities.editor.advancedsettings

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
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.SavedStateHandle
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.editor.advancedsettings.models.HostVerificationType
import ch.rmy.android.http_shortcuts.components.CertificateFingerprintTextField
import ch.rmy.android.http_shortcuts.components.Checkbox
import ch.rmy.android.http_shortcuts.components.SelectionField
import ch.rmy.android.http_shortcuts.components.SettingsButton
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.components.VariablePlaceholderTextField
import ch.rmy.android.http_shortcuts.data.enums.IpVersion
import ch.rmy.android.http_shortcuts.data.enums.ProxyType

@Composable
fun AdvancedSettingsContent(
    savedStateHandle: SavedStateHandle,
    followRedirects: Boolean,
    storeCookies: Boolean,
    keepConnectionOpen: Boolean,
    requireSpecificWifi: Boolean,
    wifiSsid: String,
    timeoutSubtitle: String,
    ipVersion: IpVersion?,
    proxyType: ProxyType?,
    proxyHost: String,
    proxyPort: String,
    proxyUsername: String,
    proxyPassword: String,
    hostVerificationEnabled: Boolean,
    hostVerificationType: HostVerificationType,
    certificateFingerprint: String,
    onFollowRedirectsChanged: (Boolean) -> Unit,
    onStoreCookiesChanged: (Boolean) -> Unit,
    onKeepConnectionOpenChanged: (Boolean) -> Unit,
    onRequireSpecificWifiChanged: (Boolean) -> Unit,
    onWifiSsidChanged: (String) -> Unit,
    onTimeoutButtonClicked: () -> Unit,
    onIpVersionChanged: (IpVersion?) -> Unit,
    onProxyTypeChanged: (ProxyType?) -> Unit,
    onProxyHostChanged: (String) -> Unit,
    onProxyPortChanged: (String) -> Unit,
    onProxyUsernameChanged: (String) -> Unit,
    onProxyPasswordChanged: (String) -> Unit,
    onHostVerificationTypeChanged: (HostVerificationType) -> Unit,
    onCertificateFingerprintChanged: (String) -> Unit,
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

        Checkbox(
            label = stringResource(R.string.label_keep_connection_open),
            checked = keepConnectionOpen,
            onCheckedChange = onKeepConnectionOpenChanged,
        )

        HorizontalDivider()

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
                        autoCorrectEnabled = false,
                    ),
                    value = wifiSsid,
                    onValueChange = onWifiSsidChanged,
                    singleLine = true,
                )
            }
        }

        HorizontalDivider()

        SettingsButton(
            title = stringResource(R.string.label_timeout),
            subtitle = timeoutSubtitle,
            onClick = onTimeoutButtonClicked,
        )

        HorizontalDivider()

        Column(
            modifier = Modifier.padding(Spacing.MEDIUM),
            verticalArrangement = Arrangement.spacedBy(Spacing.SMALL),
        ) {
            SelectionField(
                title = stringResource(R.string.label_ip_version),
                selectedKey = ipVersion,
                items = listOf(
                    null to stringResource(R.string.option_ip_version_auto),
                    IpVersion.V4 to "IPv4",
                    IpVersion.V6 to "IPv6",
                ),
                onItemSelected = onIpVersionChanged,
            )
        }

        HorizontalDivider()

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
                        savedStateHandle = savedStateHandle,
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
                        savedStateHandle = savedStateHandle,
                        username = proxyUsername,
                        onUsernameChanged = onProxyUsernameChanged,
                    )

                    ProxyPasswordField(
                        savedStateHandle = savedStateHandle,
                        password = proxyPassword,
                        onPasswordChanged = onProxyPasswordChanged,
                    )
                }
            }
        }

        HorizontalDivider()

        Column(
            modifier = Modifier.padding(Spacing.MEDIUM),
            verticalArrangement = Arrangement.spacedBy(Spacing.SMALL),
        ) {
            HostVerificationTypeSelection(
                enabled = hostVerificationEnabled,
                hostVerificationType = hostVerificationType,
                onHostVerificationTypeChanged = onHostVerificationTypeChanged,
            )

            AnimatedVisibility(visible = hostVerificationType == HostVerificationType.SELF_SIGNED) {
                CertificateFingerprintTextField(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = hostVerificationEnabled,
                    label = stringResource(R.string.label_host_verification_certificate_fingerprint),
                    placeholder = stringResource(R.string.hint_host_verification_certificate_fingerprint),
                    value = certificateFingerprint,
                    onValueChanged = onCertificateFingerprintChanged,
                )
            }
        }

        HorizontalDivider()
    }
}

@Composable
private fun HostVerificationTypeSelection(
    enabled: Boolean,
    hostVerificationType: HostVerificationType,
    onHostVerificationTypeChanged: (HostVerificationType) -> Unit,
) {
    SelectionField(
        title = stringResource(R.string.label_host_verification),
        selectedKey = hostVerificationType,
        enabled = enabled,
        items = listOf(
            HostVerificationType.DEFAULT to stringResource(R.string.option_host_verification_default),
            HostVerificationType.SELF_SIGNED to stringResource(R.string.option_host_verification_self_signed),
            HostVerificationType.TRUST_ALL to stringResource(R.string.option_host_verification_trust_all),
        ),
        onItemSelected = onHostVerificationTypeChanged,
    )
}

@Composable
private fun ProxyHostField(
    modifier: Modifier = Modifier,
    savedStateHandle: SavedStateHandle,
    host: String,
    onHostChanged: (String) -> Unit,
) {
    VariablePlaceholderTextField(
        savedStateHandle = savedStateHandle,
        key = "proxy-host-input",
        modifier = modifier,
        label = {
            Text(stringResource(R.string.label_proxy_host))
        },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            autoCorrectEnabled = false,
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
            autoCorrectEnabled = false,
            keyboardType = KeyboardType.Number,
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
    savedStateHandle: SavedStateHandle,
    modifier: Modifier = Modifier,
    username: String,
    onUsernameChanged: (String) -> Unit,
) {
    VariablePlaceholderTextField(
        savedStateHandle = savedStateHandle,
        key = "proxy-username-input",
        modifier = modifier,
        label = {
            Text(stringResource(R.string.label_proxy_username))
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
private fun ProxyPasswordField(
    savedStateHandle: SavedStateHandle,
    modifier: Modifier = Modifier,
    password: String,
    onPasswordChanged: (String) -> Unit,
) {
    VariablePlaceholderTextField(
        savedStateHandle = savedStateHandle,
        key = "proxy-password-input",
        modifier = modifier,
        label = {
            Text(stringResource(R.string.label_proxy_password))
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
