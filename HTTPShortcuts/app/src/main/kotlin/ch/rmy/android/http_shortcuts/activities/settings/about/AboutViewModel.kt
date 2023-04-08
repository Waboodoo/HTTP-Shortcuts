package ch.rmy.android.http_shortcuts.activities.settings.about

import android.app.Application
import ch.rmy.android.framework.utils.InstallUtil
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.activities.settings.acknowledgment.AcknowledgmentActivity
import ch.rmy.android.http_shortcuts.activities.settings.contact.ContactActivity
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.utils.ExternalURLs
import ch.rmy.android.http_shortcuts.utils.Settings
import ch.rmy.android.http_shortcuts.utils.VersionUtil
import javax.inject.Inject

class AboutViewModel(application: Application) : BaseViewModel<Unit, AboutViewState>(application) {

    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var versionUtil: VersionUtil

    @Inject
    lateinit var installUtil: InstallUtil

    init {
        getApplicationComponent().inject(this)
    }

    override fun initViewState() = AboutViewState(
        versionNumber = versionUtil.getVersionName(),
        fDroidVisible = !installUtil.isAppInstalledFromPlayStore(),
        changeLogDialogPermanentlyHidden = !settings.isChangeLogPermanentlyHidden,
    )

    fun onChangeLogDialogPermanentlyHiddenChanged(hidden: Boolean) {
        updateViewState {
            copy(changeLogDialogPermanentlyHidden = hidden)
        }
        settings.isChangeLogPermanentlyHidden = hidden
    }

    fun onChangeLogButtonClicked() {
        updateViewState {
            copy(changeLogDialogVisible = true)
        }
        settings.changeLogLastVersion = versionUtil.getVersionName()
    }

    fun onDocumentationButtonClicked() {
        openURL(ExternalURLs.DOCUMENTATION_PAGE)
    }

    fun onPrivacyPolicyButtonClicked() {
        openURL(ExternalURLs.PRIVACY_POLICY)
    }

    fun onContactButtonClicked() {
        openActivity(ContactActivity.IntentBuilder())
    }

    fun onTranslateButtonClicked() {
        openURL(ExternalURLs.TRANSLATION)
    }

    fun onPlayStoreButtonClicked() {
        openURL(ExternalURLs.PLAY_STORE)
    }

    fun onFDroidButtonClicked() {
        openURL(ExternalURLs.F_DROID)
    }

    fun onGithubButtonClicked() {
        openURL(ExternalURLs.GITHUB)
    }

    fun onDonateButtonClicked() {
        openURL(ExternalURLs.DONATION_PAGE)
    }

    fun onAcknowledgementButtonClicked() {
        openActivity(AcknowledgmentActivity.IntentBuilder())
    }

    fun onDialogDismissalRequested() {
        updateViewState {
            copy(changeLogDialogVisible = false)
        }
    }
}
