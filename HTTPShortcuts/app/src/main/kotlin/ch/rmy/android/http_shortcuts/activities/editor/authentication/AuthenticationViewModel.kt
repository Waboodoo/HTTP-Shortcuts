package ch.rmy.android.http_shortcuts.activities.editor.authentication

import android.app.Application
import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.editor.authentication.usecases.CopyCertificateFileUseCase
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.enums.ClientCertParams
import ch.rmy.android.http_shortcuts.data.enums.ShortcutAuthenticationType
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import ch.rmy.android.http_shortcuts.utils.ClientCertUtil
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import javax.inject.Inject

class AuthenticationViewModel(application: Application) : BaseViewModel<Unit, AuthenticationViewState>(application) {

    @Inject
    lateinit var temporaryShortcutRepository: TemporaryShortcutRepository

    @Inject
    lateinit var copyCertificateFile: CopyCertificateFileUseCase

    @Inject
    lateinit var activityProvider: ActivityProvider

    init {
        getApplicationComponent().inject(this)
    }

    override fun onInitializationStarted(data: Unit) {
        finalizeInitialization(silent = true)
    }

    override fun initViewState() = AuthenticationViewState()

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
    }

    private fun initViewStateFromShortcut(shortcut: Shortcut) {
        updateViewState {
            copy(
                authenticationType = shortcut.authenticationType,
                username = shortcut.username,
                password = shortcut.password,
                token = shortcut.authToken,
                clientCertParams = shortcut.clientCertParams,
                isClientCertButtonEnabled = !shortcut.acceptAllCertificates,
            )
        }
    }

    private fun onInitializationError(error: Throwable) {
        handleUnexpectedError(error)
        finish()
    }

    fun onAuthenticationTypeChanged(authenticationType: ShortcutAuthenticationType) {
        updateViewState {
            copy(
                authenticationType = authenticationType,
            )
        }
        launchWithProgressTracking {
            temporaryShortcutRepository.setAuthenticationType(authenticationType)
        }
    }

    fun onUsernameChanged(username: String) {
        updateViewState {
            copy(username = username)
        }

        launchWithProgressTracking {
            temporaryShortcutRepository.setUsername(username)
        }
    }

    fun onPasswordChanged(password: String) {
        updateViewState {
            copy(password = password)
        }

        launchWithProgressTracking {
            temporaryShortcutRepository.setPassword(password)
        }
    }

    fun onTokenChanged(token: String) {
        updateViewState {
            copy(token = token)
        }

        launchWithProgressTracking {
            temporaryShortcutRepository.setToken(token)
        }
    }

    fun onClientCertButtonClicked() {
        doWithViewState { viewState ->
            if (viewState.clientCertParams == null) {
                updateDialogState(AuthenticationDialogState.SelectClientCertType)
            } else {
                onClientCertParamsChanged(null)
            }
        }
    }

    fun onPickCertificateFromSystemOptionSelected() {
        updateDialogState(null)
        try {
            ClientCertUtil.promptForAlias(activityProvider.getActivity()) { alias ->
                onClientCertParamsChanged(
                    ClientCertParams.Alias(alias)
                )
            }
        } catch (e: ActivityNotFoundException) {
            showToast(R.string.error_not_supported)
        }
    }

    fun onCertificateFileSelected(file: Uri) {
        launchWithProgressTracking {
            try {
                updateDialogState(
                    AuthenticationDialogState.PasswordPromptForCertFile(fileName = copyCertificateFile(file))
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleUnexpectedError(e)
            }
        }
    }

    fun onCertPasswordConfirmed(password: String) {
        val fileName = (currentViewState?.dialogState as? AuthenticationDialogState.PasswordPromptForCertFile)?.fileName
            ?: return
        updateDialogState(null)
        onClientCertParamsChanged(
            ClientCertParams.File(fileName, password)
        )
    }

    private fun onClientCertParamsChanged(clientCertParams: ClientCertParams?) {
        updateViewState {
            copy(clientCertParams = clientCertParams)
        }
        launchWithProgressTracking {
            temporaryShortcutRepository.setClientCertParams(clientCertParams)
        }
    }

    fun onBackPressed() {
        viewModelScope.launch {
            waitForOperationsToFinish()
            finish()
        }
    }

    fun onCertificateFilePickerFailed() {
        showSnackbar(R.string.error_not_supported)
    }

    fun onDialogDismissed() {
        updateDialogState(null)
    }

    private fun updateDialogState(dialogState: AuthenticationDialogState?) {
        updateViewState {
            copy(dialogState = dialogState)
        }
    }
}
