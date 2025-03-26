package ch.rmy.android.http_shortcuts.activities.editor.authentication

import android.app.Application
import android.content.ActivityNotFoundException
import android.net.Uri
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.ViewModelScope
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.editor.authentication.usecases.CopyCertificateFileUseCase
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.enums.ClientCertParams
import ch.rmy.android.http_shortcuts.data.enums.ShortcutAuthenticationType
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import ch.rmy.android.http_shortcuts.utils.ClientCertUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

@HiltViewModel
class AuthenticationViewModel
@Inject
constructor(
    application: Application,
    private val temporaryShortcutRepository: TemporaryShortcutRepository,
    private val copyCertificateFile: CopyCertificateFileUseCase,
    private val activityProvider: ActivityProvider,
) : BaseViewModel<Unit, AuthenticationViewState>(application) {

    override suspend fun initialize(data: Unit): AuthenticationViewState {
        val shortcut = temporaryShortcutRepository.getTemporaryShortcut()
        return AuthenticationViewState(
            shortcutExecutionType = shortcut.executionType,
            authenticationType = shortcut.authenticationType,
            username = shortcut.authUsername,
            password = shortcut.authPassword,
            token = shortcut.authToken,
            clientCertParams = shortcut.clientCertParams,
            isClientCertButtonEnabled = !shortcut.url.startsWith("http://", ignoreCase = true),
        )
    }

    fun onAuthenticationTypeChanged(authenticationType: ShortcutAuthenticationType?) = runAction {
        updateViewState {
            copy(
                authenticationType = authenticationType,
            )
        }
        withProgressTracking {
            temporaryShortcutRepository.setAuthenticationType(authenticationType)
        }
    }

    fun onUsernameChanged(username: String) = runAction {
        updateViewState {
            copy(username = username)
        }
        withProgressTracking {
            temporaryShortcutRepository.setUsername(username)
        }
    }

    fun onPasswordChanged(password: String) = runAction {
        updateViewState {
            copy(password = password)
        }
        withProgressTracking {
            temporaryShortcutRepository.setPassword(password)
        }
    }

    fun onTokenChanged(token: String) = runAction {
        updateViewState {
            copy(token = token)
        }
        withProgressTracking {
            temporaryShortcutRepository.setToken(token)
        }
    }

    fun onClientCertButtonClicked() = runAction {
        if (viewState.clientCertParams == null) {
            updateDialogState(AuthenticationDialogState.SelectClientCertType)
        } else {
            onClientCertParamsChanged(null)
        }
    }

    fun onPickCertificateFromSystemOptionSelected() = runAction {
        updateDialogState(null)
        try {
            activityProvider.withActivity { activity ->
                ClientCertUtil.promptForAlias(activity) { alias ->
                    runAction {
                        onClientCertParamsChanged(
                            ClientCertParams.Alias(alias),
                        )
                    }
                }
            }
        } catch (e: ActivityNotFoundException) {
            showToast(R.string.error_not_supported)
        }
    }

    fun onCertificateFileSelected(file: Uri) = runAction {
        withProgressTracking {
            try {
                updateDialogState(
                    AuthenticationDialogState.PasswordPromptForCertFile(fileName = copyCertificateFile(file)),
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleUnexpectedError(e)
            }
        }
    }

    fun onCertPasswordConfirmed(password: String) = runAction {
        val fileName = (viewState.dialogState as? AuthenticationDialogState.PasswordPromptForCertFile)?.fileName
            ?: skipAction()
        updateDialogState(null)
        onClientCertParamsChanged(
            ClientCertParams.File(fileName, password),
        )
    }

    private suspend fun ViewModelScope<AuthenticationViewState>.onClientCertParamsChanged(clientCertParams: ClientCertParams?) {
        updateViewState {
            copy(clientCertParams = clientCertParams)
        }
        withProgressTracking {
            temporaryShortcutRepository.setClientCertParams(clientCertParams)
        }
    }

    fun onBackPressed() = runAction {
        waitForOperationsToFinish()
        closeScreen()
    }

    fun onCertificateFilePickerFailed() = runAction {
        showSnackbar(R.string.error_not_supported)
    }

    fun onDialogDismissed() = runAction {
        updateDialogState(null)
    }

    private suspend fun updateDialogState(dialogState: AuthenticationDialogState?) {
        updateViewState {
            copy(dialogState = dialogState)
        }
    }
}
