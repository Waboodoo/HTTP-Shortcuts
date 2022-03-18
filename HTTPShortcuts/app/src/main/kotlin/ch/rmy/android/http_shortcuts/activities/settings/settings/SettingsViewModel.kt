package ch.rmy.android.http_shortcuts.activities.settings.settings

import android.app.Activity
import android.app.Application
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.createIntent
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.domains.app.AppRepository
import ch.rmy.android.http_shortcuts.http.CookieManager
import org.mindrot.jbcrypt.BCrypt

class SettingsViewModel(application: Application) : BaseViewModel<Unit, SettingsViewState>(application), WithDialog {

    private val appRepository = AppRepository()

    override var dialogState: DialogState?
        get() = currentViewState.dialogState
        set(value) {
            updateViewState {
                copy(dialogState = value)
            }
        }

    override fun initViewState() = SettingsViewState()

    fun onLockAppButtonClicked() {
        showAppLockDialog()
    }

    private fun showAppLockDialog() {
        dialogState = DialogState.create {
            title(R.string.dialog_title_lock_app)
                .message(R.string.dialog_text_lock_app)
                .positive(R.string.button_lock_app)
                .textInput(allowEmpty = false, maxLength = 50) { input ->
                    onAppLockDialogSubmitted(input)
                }
                .negative(R.string.dialog_cancel)
                .build()
        }
    }

    private fun onAppLockDialogSubmitted(password: String) {
        appRepository.setLock(BCrypt.hashpw(password, BCrypt.gensalt()))
            .compose(progressMonitor.transformer())
            .subscribe(
                {
                    finish(
                        result = Activity.RESULT_OK,
                        intent = createIntent {
                            putExtra(SettingsActivity.EXTRA_APP_LOCKED, true)
                        },
                    )
                },
                { e ->
                    showSnackbar(R.string.error_generic, long = true)
                    logException(e)
                },
            )
            .attachTo(destroyer)
    }

    fun onClearCookiesButtonClicked() {
        showClearCookiesDialog()
    }

    private fun showClearCookiesDialog() {
        dialogState = DialogState.create {
            message(R.string.confirm_clear_cookies_message)
                .positive(R.string.dialog_delete) {
                    onClearCookiesDialogConfirmed()
                }
                .negative(R.string.dialog_cancel)
                .build()
        }
    }

    private fun onClearCookiesDialogConfirmed() {
        CookieManager.clearCookies(context)
        showSnackbar(R.string.message_cookies_cleared)
    }
}
