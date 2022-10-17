package ch.rmy.android.http_shortcuts.activities.editor.advancedsettings

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.utils.localization.DurationLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.activities.editor.advancedsettings.usecases.GetTimeoutDialogUseCase
import ch.rmy.android.http_shortcuts.activities.variables.VariablesActivity
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.usecases.GetVariablePlaceholderPickerDialogUseCase
import ch.rmy.android.http_shortcuts.usecases.KeepVariablePlaceholderProviderUpdatedUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class AdvancedSettingsViewModel(application: Application) : BaseViewModel<Unit, AdvancedSettingsViewState>(application), WithDialog {

    @Inject
    lateinit var temporaryShortcutRepository: TemporaryShortcutRepository

    @Inject
    lateinit var keepVariablePlaceholderProviderUpdated: KeepVariablePlaceholderProviderUpdatedUseCase

    @Inject
    lateinit var getTimeoutDialog: GetTimeoutDialogUseCase

    @Inject
    lateinit var getVariablePlaceholderPickerDialog: GetVariablePlaceholderPickerDialogUseCase

    init {
        getApplicationComponent().inject(this)
    }

    override var dialogState: DialogState?
        get() = currentViewState?.dialogState
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
        viewModelScope.launch {
            try {
                val temporaryShortcut = temporaryShortcutRepository.getTemporaryShortcut()
                initViewStateFromShortcut(temporaryShortcut)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                onInitializationError(e)
            }
        }

        viewModelScope.launch {
            keepVariablePlaceholderProviderUpdated(::emitCurrentViewState)
        }
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
        launchWithProgressTracking {
            temporaryShortcutRepository.setFollowRedirects(followRedirects)
        }
    }

    fun onAcceptAllCertificatesChanged(acceptAllCertificates: Boolean) {
        updateViewState {
            copy(acceptAllCertificates = acceptAllCertificates)
        }
        launchWithProgressTracking {
            temporaryShortcutRepository.setAcceptAllCertificates(acceptAllCertificates)
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

    private fun onTimeoutChanged(timeout: Duration) {
        updateViewState {
            copy(timeout = timeout)
        }
        launchWithProgressTracking {
            temporaryShortcutRepository.setTimeout(timeout)
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

    fun onProxyPortChanged(proxyPort: Int?) {
        updateViewState {
            copy(proxyPort = proxyPort?.toString() ?: "")
        }
        launchWithProgressTracking {
            temporaryShortcutRepository.setProxyPort(proxyPort)
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
        showTimeoutDialog()
    }

    private fun showTimeoutDialog() {
        doWithViewState { viewState ->
            dialogState = getTimeoutDialog(
                viewState.timeout,
                getLabel = { duration ->
                    DurationLocalizable(duration)
                },
                onTimeoutChanged = ::onTimeoutChanged,
            )
        }
    }

    fun onBackPressed() {
        viewModelScope.launch {
            waitForOperationsToFinish()
            finish()
        }
    }

    fun onProxyHostVariableButtonClicked() {
        dialogState = getVariablePlaceholderPickerDialog.invoke(
            onVariableSelected = {
                emitEvent(AdvancedSettingsEvent.InsertVariablePlaceholder(it))
            },
            onEditVariableButtonClicked = {
                openActivity(
                    VariablesActivity.IntentBuilder()
                )
            },
        )
    }
}
