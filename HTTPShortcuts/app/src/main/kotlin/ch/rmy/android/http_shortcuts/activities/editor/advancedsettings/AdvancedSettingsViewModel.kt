package ch.rmy.android.http_shortcuts.activities.editor.advancedsettings

import android.app.Application
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.utils.localization.DurationLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.activities.editor.advancedsettings.usecases.GetTimeoutDialogUseCase
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class AdvancedSettingsViewModel(application: Application) : BaseViewModel<Unit, AdvancedSettingsViewState>(application), WithDialog {

    private val temporaryShortcutRepository = TemporaryShortcutRepository()
    private val variableRepository = VariableRepository()
    private val getTimeoutDialog = GetTimeoutDialogUseCase()

    override var dialogState: DialogState?
        get() = currentViewState.dialogState
        set(value) {
            updateViewState {
                copy(dialogState = value)
            }
        }

    override fun onInitializationStarted(data: Unit) {
        finalizeInitialization(silent = true)
    }

    override fun initViewState() = AdvancedSettingsViewState()

    override fun onInitialized() {
        temporaryShortcutRepository.getTemporaryShortcut()
            .subscribe(
                ::initViewStateFromShortcut,
                ::onInitializationError,
            )
            .attachTo(destroyer)

        variableRepository.getObservableVariables()
            .subscribe { variables ->
                updateViewState {
                    copy(variables = variables)
                }
            }
            .attachTo(destroyer)
    }

    private fun initViewStateFromShortcut(shortcut: ShortcutModel) {
        updateViewState {
            copy(
                followRedirects = shortcut.followRedirects,
                acceptAllCertificates = shortcut.acceptAllCertificates,
                acceptCookies = shortcut.acceptCookies,
                timeout = shortcut.timeout.milliseconds,
                proxyHost = shortcut.proxyHost ?: "",
                proxyPort = shortcut.proxyPort?.toString() ?: "",
                wifiSsid = shortcut.wifiSsid,
            )
        }
    }

    private fun onInitializationError(error: Throwable) {
        handleUnexpectedError(error)
        finish()
    }

    fun onFollowRedirectsChanged(followRedirects: Boolean) {
        updateViewState {
            copy(followRedirects = followRedirects)
        }
        performOperation(
            temporaryShortcutRepository.setFollowRedirects(followRedirects)
        )
    }

    fun onAcceptAllCertificatesChanged(acceptAllCertificates: Boolean) {
        updateViewState {
            copy(acceptAllCertificates = acceptAllCertificates)
        }
        performOperation(
            temporaryShortcutRepository.setAcceptAllCertificates(acceptAllCertificates)
        )
    }

    fun onAcceptCookiesChanged(acceptCookies: Boolean) {
        updateViewState {
            copy(acceptCookies = acceptCookies)
        }
        performOperation(
            temporaryShortcutRepository.setAcceptCookies(acceptCookies)
        )
    }

    fun onTimeoutChanged(timeout: Duration) {
        updateViewState {
            copy(timeout = timeout)
        }
        performOperation(
            temporaryShortcutRepository.setTimeout(timeout)
        )
    }

    fun onProxyHostChanged(proxyHost: String) {
        updateViewState {
            copy(proxyHost = proxyHost)
        }
        performOperation(
            temporaryShortcutRepository.setProxyHost(proxyHost)
        )
    }

    fun onProxyPortChanged(proxyPort: Int?) {
        updateViewState {
            copy(proxyPort = proxyPort?.toString() ?: "")
        }
        performOperation(
            temporaryShortcutRepository.setProxyPort(proxyPort)
        )
    }

    fun onWifiSsidChanged(ssid: String) {
        updateViewState {
            copy(wifiSsid = ssid)
        }
        performOperation(
            temporaryShortcutRepository.setWifiSsid(ssid)
        )
    }

    fun onTimeoutButtonClicked() {
        showTimeoutDialog()
    }

    private fun showTimeoutDialog() {
        dialogState = getTimeoutDialog(
            currentViewState.timeout,
            getLabel = { duration ->
                DurationLocalizable(duration)
            },
            onTimeoutChanged = ::onTimeoutChanged,
        )
    }

    fun onBackPressed() {
        waitForOperationsToFinish {
            finish()
        }
    }
}
