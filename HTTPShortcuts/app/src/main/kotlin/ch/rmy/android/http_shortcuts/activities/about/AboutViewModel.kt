package ch.rmy.android.http_shortcuts.activities.about

import android.app.Application
import ch.rmy.android.framework.utils.ClipboardUtil
import ch.rmy.android.framework.utils.InstallUtil
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.settings.DeviceLocalPreferences
import ch.rmy.android.http_shortcuts.data.settings.UserPreferences
import ch.rmy.android.http_shortcuts.logging.Logging
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import ch.rmy.android.http_shortcuts.utils.ExternalURLs
import ch.rmy.android.http_shortcuts.utils.VersionUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AboutViewModel
@Inject
constructor(
    application: Application,
    private val userPreferences: UserPreferences,
    private val deviceLocalPreferences: DeviceLocalPreferences,
    private val versionUtil: VersionUtil,
    private val installUtil: InstallUtil,
    private val clipboardUtil: ClipboardUtil,
) : BaseViewModel<Unit, AboutViewState>(application) {

    override suspend fun initialize(data: Unit): AboutViewState =
        AboutViewState(
            versionNumber = getFormattedVersionNumber(),
            fDroidVisible = !installUtil.isAppInstalledFromPlayStore(),
            changeLogDialogPermanentlyHidden = deviceLocalPreferences.isChangeLogPermanentlyHidden,
            deviceId = deviceLocalPreferences.deviceId,
            crashReportingAllowed = Logging.supportsCrashReporting && userPreferences.isCrashReportingAllowed,
        )

    private fun getFormattedVersionNumber() =
        "${versionUtil.getVersionName()} (b${versionUtil.getBuildNumber()})"

    fun onChangeLogDialogPermanentlyHiddenChanged(hidden: Boolean) = runAction {
        updateViewState {
            copy(changeLogDialogPermanentlyHidden = hidden)
        }
        deviceLocalPreferences.isChangeLogPermanentlyHidden = hidden
    }

    fun onChangeLogButtonClicked() = runAction {
        updateViewState {
            copy(changeLogDialogVisible = true)
        }
        deviceLocalPreferences.changeLogLastVersion = versionUtil.getVersionName()
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

    fun onDeviceIdButtonClicked() = runAction {
        clipboardUtil.copyToClipboard(deviceLocalPreferences.deviceId)
        showSnackbar(R.string.message_device_id_copied)
    }

    fun onWebsiteButtonClicked() = runAction {
        openURL(ExternalURLs.BASE_URL)
    }

    fun onRedditButtonClicked() = runAction {
        openURL(ExternalURLs.REDDIT)
    }

    fun onDialogDismissalRequested() = runAction {
        updateViewState {
            copy(changeLogDialogVisible = false)
        }
    }
}
