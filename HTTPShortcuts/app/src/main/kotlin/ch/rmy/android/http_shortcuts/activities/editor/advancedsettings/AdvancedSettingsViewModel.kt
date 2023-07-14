package ch.rmy.android.http_shortcuts.activities.editor.advancedsettings

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.activities.editor.advancedsettings.models.HostVerificationType
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.enums.ProxyType
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.isValidCertificateFingerprint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class AdvancedSettingsViewModel(application: Application) : BaseViewModel<Unit, AdvancedSettingsViewState>(application) {

    @Inject
    lateinit var temporaryShortcutRepository: TemporaryShortcutRepository

    init {
        getApplicationComponent().inject(this)
    }

    private lateinit var initialViewState: AdvancedSettingsViewState

    override fun onInitializationStarted(data: Unit) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val temporaryShortcut = temporaryShortcutRepository.getTemporaryShortcut()
                initialViewState = createInitialViewState(temporaryShortcut)
                withContext(Dispatchers.Main) {
                    finalizeInitialization()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onInitializationError(e)
                }
            }
        }
    }

    override fun initViewState() = initialViewState

    private fun createInitialViewState(shortcut: Shortcut) = AdvancedSettingsViewState(
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

    private fun Shortcut.getHostVerificationType() =
        when {
            acceptAllCertificates -> HostVerificationType.TRUST_ALL
            certificateFingerprint.isNotEmpty() -> HostVerificationType.SELF_SIGNED
            else -> HostVerificationType.DEFAULT
        }

    private fun onInitializationError(error: Throwable) {
        handleUnexpectedError(error)
        finish()
    }

    fun onFollowRedirectsChanged(followRedirects: Boolean) {
        updateViewState {
            copy(followRedirects = followRedirects)
        }
        launchWithProgressTracking {
            temporaryShortcutRepository.setFollowRedirects(followRedirects)
        }
    }

    fun onHostVerificationTypeChanged(hostVerificationType: HostVerificationType) {
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
        launchWithProgressTracking {
            if (hostVerificationType != HostVerificationType.SELF_SIGNED) {
                temporaryShortcutRepository.setCertificateFingerprint("")
            }
            temporaryShortcutRepository.setAcceptAllCertificates(hostVerificationType == HostVerificationType.TRUST_ALL)
        }
    }

    fun onCertificateFingerprintChanged(certificateFingerprint: String) {
        updateViewState {
            copy(certificateFingerprint = certificateFingerprint)
        }
        if (certificateFingerprint.isEmpty() || certificateFingerprint.isValidCertificateFingerprint()) {
            launchWithProgressTracking {
                temporaryShortcutRepository.setCertificateFingerprint(certificateFingerprint)
            }
        }
    }

    fun onAcceptCookiesChanged(acceptCookies: Boolean) {
        updateViewState {
            copy(acceptCookies = acceptCookies)
        }
        launchWithProgressTracking {
            temporaryShortcutRepository.setAcceptCookies(acceptCookies)
        }
    }

    fun onRequireSpecificWifiChanged(requireWifi: Boolean) {
        val ssid = currentViewState?.wifiSsid ?: return
        updateViewState {
            copy(requireSpecificWifi = requireWifi)
        }
        launchWithProgressTracking {
            temporaryShortcutRepository.setWifiSsid(if (requireWifi) ssid else "")
        }
    }

    fun onTimeoutChanged(timeout: Duration) {
        updateViewState {
            copy(
                timeout = timeout,
                dialogState = null,
            )
        }
        launchWithProgressTracking {
            temporaryShortcutRepository.setTimeout(timeout)
        }
    }

    fun onProxyTypeChanged(proxyType: ProxyType?) {
        val viewState = currentViewState ?: return
        updateViewState {
            copy(proxyType = proxyType)
        }
        launchWithProgressTracking {
            if (proxyType != null) {
                temporaryShortcutRepository.setProxyType(proxyType)
                temporaryShortcutRepository.setProxyHost(viewState.proxyHost)
            } else {
                temporaryShortcutRepository.setProxyType(ProxyType.HTTP)
                temporaryShortcutRepository.setProxyHost("")
            }
        }
    }

    fun onProxyHostChanged(proxyHost: String) {
        updateViewState {
            copy(proxyHost = proxyHost)
        }
        launchWithProgressTracking {
            temporaryShortcutRepository.setProxyHost(proxyHost)
        }
    }

    fun onProxyPortChanged(proxyPort: String) {
        updateViewState {
            copy(proxyPort = proxyPort)
        }
        launchWithProgressTracking {
            temporaryShortcutRepository.setProxyPort(proxyPort.toIntOrNull())
        }
    }

    fun onProxyUsernameChanged(proxyUsername: String) {
        updateViewState {
            copy(proxyUsername = proxyUsername)
        }
        launchWithProgressTracking {
            temporaryShortcutRepository.setProxyUsername(proxyUsername)
        }
    }

    fun onProxyPasswordChanged(proxyPassword: String) {
        updateViewState {
            copy(proxyPassword = proxyPassword)
        }
        launchWithProgressTracking {
            temporaryShortcutRepository.setProxyPassword(proxyPassword)
        }
    }

    fun onWifiSsidChanged(ssid: String) {
        updateViewState {
            copy(wifiSsid = ssid)
        }
        launchWithProgressTracking {
            temporaryShortcutRepository.setWifiSsid(ssid)
        }
    }

    fun onTimeoutButtonClicked() {
        val timeout = currentViewState?.timeout ?: return
        updateDialogState(
            AdvancedSettingsDialogState.TimeoutPicker(timeout)
        )
    }

    fun onBackPressed() {
        viewModelScope.launch {
            waitForOperationsToFinish()
            finish()
        }
    }

    fun onDialogDismissed() {
        updateDialogState(null)
    }

    private fun updateDialogState(dialogState: AdvancedSettingsDialogState?) {
        updateViewState {
            copy(dialogState = dialogState)
        }
    }
}
