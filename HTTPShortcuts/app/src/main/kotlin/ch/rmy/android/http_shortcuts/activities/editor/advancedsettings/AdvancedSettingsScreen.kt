package ch.rmy.android.http_shortcuts.activities.editor.advancedsettings

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.ScreenScope
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.bindViewModel
import ch.rmy.android.http_shortcuts.extensions.localize

@Composable
fun ScreenScope.AdvancedSettingsScreen() {
    val (viewModel, state) = bindViewModel<AdvancedSettingsViewState, AdvancedSettingsViewModel>()

    BackHandler {
        viewModel.onBackPressed()
    }

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.label_advanced_technical_settings),
    ) { viewState ->
        AdvancedSettingsContent(
            followRedirects = viewState.followRedirects,
            storeCookies = viewState.acceptCookies,
            requireSpecificWifi = viewState.requireSpecificWifi,
            wifiSsid = viewState.wifiSsid,
            timeoutSubtitle = viewState.timeoutSubtitle.localize(),
            proxyType = viewState.proxyType,
            proxyHost = viewState.proxyHost,
            proxyPort = viewState.proxyPort,
            proxyUsername = viewState.proxyUsername,
            proxyPassword = viewState.proxyPassword,
            acceptAllCertificates = viewState.acceptAllCertificates,
            onFollowRedirectsChanged = viewModel::onFollowRedirectsChanged,
            onStoreCookiesChanged = viewModel::onAcceptCookiesChanged,
            onRequireSpecificWifiChanged = viewModel::onRequireSpecificWifiChanged,
            onWifiSsidChanged = viewModel::onWifiSsidChanged,
            onTimeoutButtonClicked = viewModel::onTimeoutButtonClicked,
            onProxyTypeChanged = viewModel::onProxyTypeChanged,
            onProxyHostChanged = viewModel::onProxyHostChanged,
            onProxyPortChanged = viewModel::onProxyPortChanged,
            onProxyUsernameChanged = viewModel::onProxyUsernameChanged,
            onProxyPasswordChanged = viewModel::onProxyPasswordChanged,
            onAcceptAllCertificates = viewModel::onAcceptAllCertificatesChanged,
        )
    }

    AdvancedSettingsDialogs(
        dialogState = state?.dialogState,
        onTimeoutConfirmed = viewModel::onTimeoutChanged,
        onDismissed = viewModel::onDialogDismissed,
    )
}
