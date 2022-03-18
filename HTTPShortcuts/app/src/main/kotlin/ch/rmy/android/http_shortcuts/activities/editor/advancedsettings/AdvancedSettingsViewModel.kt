package ch.rmy.android.http_shortcuts.activities.editor.advancedsettings

import android.app.Application
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.utils.localization.DurationLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.enums.ClientCertParams
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class AdvancedSettingsViewModel(application: Application) : BaseViewModel<Unit, AdvancedSettingsViewState>(application), WithDialog {

    private val temporaryShortcutRepository = TemporaryShortcutRepository()
    private val variableRepository = VariableRepository()

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
                clientCertParams = shortcut.clientCertParams,
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

    fun onClientCertParamsChanged(clientCertParams: ClientCertParams?) {
        updateViewState {
            copy(clientCertParams = clientCertParams)
        }
        performOperation(
            temporaryShortcutRepository.setClientCertParams(clientCertParams)
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

    fun onClientCertButtonClicked() {
        if (currentViewState.clientCertParams == null) {
            showClientCertDialog()
        } else {
            onClientCertParamsChanged(null)
        }
    }

    private fun showClientCertDialog() {
        dialogState = DialogState.create {
            title(R.string.title_client_cert)
                .item(R.string.label_client_cert_from_os, descriptionRes = R.string.label_client_cert_from_os_subtitle) {
                    promptForClientCertAlias()
                }
                .item(R.string.label_client_cert_from_file, descriptionRes = R.string.label_client_cert_from_file_subtitle) {
                    openCertificateFilePicker()
                }
                .build()
        }
    }

    private fun promptForClientCertAlias() {
        emitEvent(AdvancedSettingsEvent.PromptForClientCertAlias)
    }

    private fun openCertificateFilePicker() {
        emitEvent(AdvancedSettingsEvent.OpenCertificateFilePicker)
    }

    fun onTimeoutButtonClicked() {
        emitEvent(
            AdvancedSettingsEvent.ShowTimeoutDialog(currentViewState.timeout) { duration ->
                DurationLocalizable(duration)
            }
        )
    }

    fun onBackPressed() {
        waitForOperationsToFinish {
            finish()
        }
    }
}
