package ch.rmy.android.http_shortcuts.activities.editor.advancedsettings

import android.app.Application
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.activities.editor.advancedsettings.models.HostVerificationType
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.enums.ProxyType
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.isValidCertificateFingerprint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class AdvancedSettingsViewModel
@Inject
constructor(
    application: Application,
    private val temporaryShortcutRepository: TemporaryShortcutRepository,
) : BaseViewModel<Unit, AdvancedSettingsViewState>(application) {

    override suspend fun initialize(data: Unit): AdvancedSettingsViewState {
        val shortcut = temporaryShortcutRepository.getTemporaryShortcut()
        return AdvancedSettingsViewState(
            followRedirects = shortcut.followRedirects,
            certificateFingerprint = shortcut.certificateFingerprint,
            hostVerificationEnabled = !shortcut.url.startsWith("http:", ignoreCase = true),
            hostVerificationType = shortcut.getHostVerificationType(),
            acceptCookies = shortcut.acceptCookies,
            timeout = shortcut.timeout.milliseconds,
            proxyType = shortcut.proxyType.takeUnless { shortcut.proxyHost.isNullOrEmpty() },
            proxyHost = shortcut.proxyHost ?: "",
            proxyPort = shortcut.proxyPort?.toString() ?: "",
            proxyUsername = shortcut.proxyUsername ?: "",
            proxyPassword = shortcut.proxyPassword ?: "",
            requireSpecificWifi = shortcut.wifiSsid.isNotEmpty(),
            wifiSsid = shortcut.wifiSsid,
        )
    }

    private fun Shortcut.getHostVerificationType() =
        when {
            acceptAllCertificates -> HostVerificationType.TRUST_ALL
            certificateFingerprint.isNotEmpty() -> HostVerificationType.SELF_SIGNED
            else -> HostVerificationType.DEFAULT
        }

    fun onFollowRedirectsChanged(followRedirects: Boolean) = runAction {
        updateViewState {
            copy(followRedirects = followRedirects)
        }
        withProgressTracking {
            temporaryShortcutRepository.setFollowRedirects(followRedirects)
        }
    }

    fun onHostVerificationTypeChanged(hostVerificationType: HostVerificationType) = runAction {
        updateViewState {
            copy(
                hostVerificationType = hostVerificationType,
                certificateFingerprint = when (hostVerificationType) {
                    HostVerificationType.DEFAULT,
                    HostVerificationType.TRUST_ALL,
                    -> ""
                    HostVerificationType.SELF_SIGNED -> certificateFingerprint
                }
            )
        }
        withProgressTracking {
            if (hostVerificationType != HostVerificationType.SELF_SIGNED) {
                temporaryShortcutRepository.setCertificateFingerprint("")
            }
            temporaryShortcutRepository.setAcceptAllCertificates(hostVerificationType == HostVerificationType.TRUST_ALL)
        }
    }

    fun onCertificateFingerprintChanged(certificateFingerprint: String) = runAction {
        updateViewState {
            copy(certificateFingerprint = certificateFingerprint)
        }
        if (certificateFingerprint.isEmpty() || certificateFingerprint.isValidCertificateFingerprint()) {
            withProgressTracking {
                temporaryShortcutRepository.setCertificateFingerprint(certificateFingerprint)
            }
        }
    }

    fun onAcceptCookiesChanged(acceptCookies: Boolean) = runAction {
        updateViewState {
            copy(acceptCookies = acceptCookies)
        }
        withProgressTracking {
            temporaryShortcutRepository.setAcceptCookies(acceptCookies)
        }
    }

    fun onRequireSpecificWifiChanged(requireWifi: Boolean) = runAction {
        val ssid = viewState.wifiSsid
        updateViewState {
            copy(requireSpecificWifi = requireWifi)
        }
        withProgressTracking {
            temporaryShortcutRepository.setWifiSsid(if (requireWifi) ssid else "")
        }
    }

    fun onTimeoutChanged(timeout: Duration) = runAction {
        updateViewState {
            copy(
                timeout = timeout,
                dialogState = null,
            )
        }
        withProgressTracking {
            temporaryShortcutRepository.setTimeout(timeout)
        }
    }

    fun onProxyTypeChanged(proxyType: ProxyType?) = runAction {
        updateViewState {
            copy(proxyType = proxyType)
        }
        withProgressTracking {
            if (proxyType != null) {
                temporaryShortcutRepository.setProxyType(proxyType)
                temporaryShortcutRepository.setProxyHost(viewState.proxyHost)
            } else {
                temporaryShortcutRepository.setProxyType(ProxyType.HTTP)
                temporaryShortcutRepository.setProxyHost("")
            }
        }
    }

    fun onProxyHostChanged(proxyHost: String) = runAction {
        updateViewState {
            copy(proxyHost = proxyHost)
        }
        withProgressTracking {
            temporaryShortcutRepository.setProxyHost(proxyHost)
        }
    }

    fun onProxyPortChanged(proxyPort: String) = runAction {
        updateViewState {
            copy(proxyPort = proxyPort)
        }
        withProgressTracking {
            temporaryShortcutRepository.setProxyPort(proxyPort.toIntOrNull())
        }
    }

    fun onProxyUsernameChanged(proxyUsername: String) = runAction {
        updateViewState {
            copy(proxyUsername = proxyUsername)
        }
        withProgressTracking {
            temporaryShortcutRepository.setProxyUsername(proxyUsername)
        }
    }

    fun onProxyPasswordChanged(proxyPassword: String) = runAction {
        updateViewState {
            copy(proxyPassword = proxyPassword)
        }
        withProgressTracking {
            temporaryShortcutRepository.setProxyPassword(proxyPassword)
        }
    }

    fun onWifiSsidChanged(ssid: String) = runAction {
        updateViewState {
            copy(wifiSsid = ssid)
        }
        withProgressTracking {
            temporaryShortcutRepository.setWifiSsid(ssid)
        }
    }

    fun onTimeoutButtonClicked() = runAction {
        updateDialogState(
            AdvancedSettingsDialogState.TimeoutPicker(viewState.timeout)
        )
    }

    fun onBackPressed() = runAction {
        waitForOperationsToFinish()
        closeScreen()
    }

    fun onDialogDismissed() = runAction {
        updateDialogState(null)
    }

    private suspend fun updateDialogState(dialogState: AdvancedSettingsDialogState?) {
        updateViewState {
            copy(dialogState = dialogState)
        }
    }
}
