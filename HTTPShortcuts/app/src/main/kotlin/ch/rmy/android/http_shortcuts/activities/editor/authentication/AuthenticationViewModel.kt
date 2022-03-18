package ch.rmy.android.http_shortcuts.activities.editor.authentication

import android.app.Application
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.enums.ShortcutAuthenticationType
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel

class AuthenticationViewModel(application: Application) : BaseViewModel<Unit, AuthenticationViewState>(application) {

    private val temporaryShortcutRepository = TemporaryShortcutRepository()
    private val variableRepository = VariableRepository()

    override fun onInitializationStarted(data: Unit) {
        finalizeInitialization(silent = true)
    }

    override fun initViewState() = AuthenticationViewState()

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
                authenticationType = shortcut.authenticationType,
                username = shortcut.username,
                password = shortcut.password,
                token = shortcut.authToken,
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
        performOperation(
            temporaryShortcutRepository.setAuthenticationType(authenticationType)
        )
    }

    fun onUsernameChanged(username: String) {
        updateViewState {
            copy(username = username)
        }

        performOperation(
            temporaryShortcutRepository.setUsername(username)
        )
    }

    fun onPasswordChanged(password: String) {
        updateViewState {
            copy(password = password)
        }

        performOperation(
            temporaryShortcutRepository.setPassword(password)
        )
    }

    fun onTokenChanged(token: String) {
        updateViewState {
            copy(token = token)
        }

        performOperation(
            temporaryShortcutRepository.setToken(token)
        )
    }

    fun onBackPressed() {
        waitForOperationsToFinish {
            finish()
        }
    }
}
