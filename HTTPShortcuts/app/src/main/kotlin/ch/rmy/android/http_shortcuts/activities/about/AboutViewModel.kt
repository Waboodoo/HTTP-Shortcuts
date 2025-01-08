package ch.rmy.android.http_shortcuts.activities.about

import android.app.Application
import ch.rmy.android.framework.utils.InstallUtil
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import ch.rmy.android.http_shortcuts.utils.ExternalURLs
import ch.rmy.android.http_shortcuts.utils.Settings
import ch.rmy.android.http_shortcuts.utils.VersionUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AboutViewModel
@Inject
constructor(
    application: Application,
    private val settings: Settings,
    private val versionUtil: VersionUtil,
    private val installUtil: InstallUtil,
) : BaseViewModel<Unit, AboutViewState>(application) {

    override suspend fun initialize(data: Unit): AboutViewState =
        AboutViewState(
            versionNumber = versionUtil.getVersionName(),
            fDroidVisible = !installUtil.isAppInstalledFromPlayStore(),
            changeLogDialogPermanentlyHidden = settings.isChangeLogPermanentlyHidden,
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
        navigate(NavigationDestination.Contact)
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

    fun onGitHubButtonClicked() = runAction {
        openURL(ExternalURLs.GITHUB)
    }

    fun onDonateButtonClicked() = runAction {
        openURL(ExternalURLs.DONATION_PAGE)
    }

    fun onAcknowledgementButtonClicked() = runAction {
        navigate(NavigationDestination.Acknowledgment)
    }

    fun onDialogDismissalRequested() = runAction {
        updateViewState {
            copy(changeLogDialogVisible = false)
        }
    }
}
