package ch.rmy.android.http_shortcuts.activities.about

import android.app.Application
import ch.rmy.android.framework.utils.InstallUtil
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.activities.acknowledgment.AcknowledgmentActivity
import ch.rmy.android.http_shortcuts.activities.contact.ContactActivity
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

    override suspend fun initialize(data: Unit): AboutViewState =
        AboutViewState(
            versionNumber = versionUtil.getVersionName(),
            fDroidVisible = !installUtil.isAppInstalledFromPlayStore(),
            changeLogDialogPermanentlyHidden = !settings.isChangeLogPermanentlyHidden,
        )

    fun onChangeLogDialogPermanentlyHiddenChanged(hidden: Boolean) = runAction {
        updateViewState {
            copy(changeLogDialogPermanentlyHidden = hidden)
        }
        settings.isChangeLogPermanentlyHidden = hidden
    }

    fun onChangeLogButtonClicked() = runAction {
        updateViewState {
            copy(changeLogDialogVisible = true)
        }
        settings.changeLogLastVersion = versionUtil.getVersionName()
    }

    fun onDocumentationButtonClicked() = runAction {
        openURL(ExternalURLs.DOCUMENTATION_PAGE)
    }

    fun onPrivacyPolicyButtonClicked() = runAction {
        openURL(ExternalURLs.PRIVACY_POLICY)
    }

    fun onContactButtonClicked() = runAction {
        openActivity(ContactActivity.IntentBuilder())
    }

    fun onTranslateButtonClicked() = runAction {
        openURL(ExternalURLs.TRANSLATION)
    }

    fun onPlayStoreButtonClicked() = runAction {
        openURL(ExternalURLs.PLAY_STORE)
    }

    fun onFDroidButtonClicked() = runAction {
        openURL(ExternalURLs.F_DROID)
    }

    fun onGithubButtonClicked() = runAction {
        openURL(ExternalURLs.GITHUB)
    }

    fun onDonateButtonClicked() = runAction {
        openURL(ExternalURLs.DONATION_PAGE)
    }

    fun onAcknowledgementButtonClicked() = runAction {
        openActivity(AcknowledgmentActivity.IntentBuilder())
    }

    fun onDialogDismissalRequested() = runAction {
        updateViewState {
            copy(changeLogDialogVisible = false)
        }
    }
}
