package ch.rmy.android.http_shortcuts.activities.editor.advancedsettings

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.SavedStateHandle
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.bindViewModel
import ch.rmy.android.http_shortcuts.extensions.localize

@Composable
fun AdvancedSettingsScreen(
    savedStateHandle: SavedStateHandle,
) {
    val (viewModel, state) = bindViewModel<AdvancedSettingsViewState, AdvancedSettingsViewModel>()

    BackHandler(state != null) {
        viewModel.onBackPressed()
    }

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.label_advanced_technical_settings),
    ) { viewState ->
        AdvancedSettingsContent(
            savedStateHandle = savedStateHandle,
            followRedirects = viewState.followRedirects,
            storeCookies = viewState.acceptCookies,
            keepConnectionOpen = viewState.keepConnectionOpen,
            requireSpecificWifi = viewState.requireSpecificWifi,
            wifiSsid = viewState.wifiSsid,
            timeoutSubtitle = viewState.timeoutSubtitle.localize(),
            ipVersion = viewState.ipVersion,
            proxyType = viewState.proxyType,
            proxyHost = viewState.proxyHost,
            proxyPort = viewState.proxyPort,
            proxyUsername = viewState.proxyUsername,
            proxyPassword = viewState.proxyPassword,
            hostVerificationEnabled = viewState.hostVerificationEnabled,
            hostVerificationType = viewState.hostVerificationType,
            certificateFingerprint = viewState.certificateFingerprint,
            onFollowRedirectsChanged = viewModel::onFollowRedirectsChanged,
            onStoreCookiesChanged = viewModel::onAcceptCookiesChanged,
            onKeepConnectionOpenChanged = viewModel::onKeepConnectionOpenChanged,
            onRequireSpecificWifiChanged = viewModel::onRequireSpecificWifiChanged,
            onWifiSsidChanged = viewModel::onWifiSsidChanged,
            onTimeoutButtonClicked = viewModel::onTimeoutButtonClicked,
            onIpVersionChanged = viewModel::onIpVersionChanged,
            onProxyTypeChanged = viewModel::onProxyTypeChanged,
            onProxyHostChanged = viewModel::onProxyHostChanged,
            onProxyPortChanged = viewModel::onProxyPortChanged,
            onProxyUsernameChanged = viewModel::onProxyUsernameChanged,
            onProxyPasswordChanged = viewModel::onProxyPasswordChanged,
            onHostVerificationTypeChanged = viewModel::onHostVerificationTypeChanged,
            onCertificateFingerprintChanged = viewModel::onCertificateFingerprintChanged,
        )
    }

    AdvancedSettingsDialogs(
        dialogState = state?.dialogState,
        onTimeoutConfirmed = viewModel::onTimeoutChanged,
        onDismissed = viewModel::onDialogDismissed,
    )
}
