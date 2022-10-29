package ch.rmy.android.http_shortcuts.activities.settings.settings

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.app.AppRepository
import ch.rmy.android.http_shortcuts.extensions.createDialogState
import ch.rmy.android.http_shortcuts.http.CookieManager
import ch.rmy.android.http_shortcuts.usecases.GetToolbarTitleChangeDialogUseCase
import ch.rmy.android.http_shortcuts.utils.LocaleHelper
import ch.rmy.android.http_shortcuts.utils.Settings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt
import javax.inject.Inject

class SettingsViewModel(application: Application) : BaseViewModel<Unit, SettingsViewState>(application), WithDialog {

    @Inject
    lateinit var appRepository: AppRepository

    @Inject
    lateinit var getToolbarTitleChangeDialog: GetToolbarTitleChangeDialogUseCase

    @Inject
    lateinit var localeHelper: LocaleHelper

    @Inject
    lateinit var cookieManager: CookieManager

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

    override fun initViewState() = SettingsViewState()

    fun onLockAppButtonClicked() {
        showAppLockDialog()
    }

    private fun showAppLockDialog() {
        dialogState = createDialogState {
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
        launchWithProgressTracking {
            try {
                appRepository.setLock(BCrypt.hashpw(password, BCrypt.gensalt()))
                finishWithOkResult(
                    SettingsActivity.OpenSettings.createResult(appLocked = true),
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                showSnackbar(R.string.error_generic, long = true)
                logException(e)
            }
        }
    }

    fun onClearCookiesButtonClicked() {
        showClearCookiesDialog()
    }

    private fun showClearCookiesDialog() {
        dialogState = createDialogState {
            message(R.string.confirm_clear_cookies_message)
                .positive(R.string.dialog_delete) {
                    onClearCookiesDialogConfirmed()
                }
                .negative(R.string.dialog_cancel)
                .build()
        }
    }

    private fun onClearCookiesDialogConfirmed() {
        cookieManager.clearCookies()
        showSnackbar(R.string.message_cookies_cleared)
    }

    fun onChangeTitleButtonClicked() {
        viewModelScope.launch {
            showToolbarTitleChangeDialog(
                appRepository.getToolbarTitle()
            )
        }
    }

    private fun showToolbarTitleChangeDialog(oldTitle: String) {
        dialogState = getToolbarTitleChangeDialog(::onToolbarTitleChangeSubmitted, oldTitle)
    }

    private fun onToolbarTitleChangeSubmitted(newTitle: String) {
        viewModelScope.launch {
            appRepository.setToolbarTitle(newTitle)
            showSnackbar(R.string.message_title_changed)
        }
    }

    fun onLanguageChanged(newLanguage: String) {
        localeHelper.applyLocale(newLanguage.takeUnless { it == Settings.LANGUAGE_DEFAULT })
    }

    fun onAddQuickSettingsTileButtonClicked() {
        emitEvent(SettingsEvent.AddQuickSettingsTile)
    }
}
