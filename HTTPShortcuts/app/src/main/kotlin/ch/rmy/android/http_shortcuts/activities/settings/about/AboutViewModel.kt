package ch.rmy.android.http_shortcuts.activities.settings.about

import android.app.Application
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.usecases.GetChangeLogDialogUseCase
import ch.rmy.android.http_shortcuts.utils.ExternalURLs
import ch.rmy.android.http_shortcuts.utils.Settings
import javax.inject.Inject

class AboutViewModel(application: Application) : BaseViewModel<Unit, AboutViewState>(application), WithDialog {

    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var getChangeLogDialog: GetChangeLogDialogUseCase

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

    override fun initViewState() = AboutViewState()

    fun onChangeLogButtonClicked() {
        updateViewState {
            copy(dialogState = getChangeLogDialog())
        }
    }

    fun onDocumentationButtonClicked() {
        openURL(ExternalURLs.DOCUMENTATION_PAGE)
    }

    fun onPrivacyPolicyButtonClicked() {
        openURL(ExternalURLs.PRIVACY_POLICY)
    }
}
