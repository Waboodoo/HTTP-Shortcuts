package ch.rmy.android.http_shortcuts.activities.main.usecases

import androidx.annotation.CheckResult
import ch.rmy.android.framework.utils.WebViewChecker
import ch.rmy.android.http_shortcuts.data.settings.DeviceLocalPreferences
import ch.rmy.android.http_shortcuts.utils.VersionUtil
import javax.inject.Inject

class ShouldShowChangeLogDialogUseCase
@Inject
constructor(
    private val deviceLocalPreferences: DeviceLocalPreferences,
    private val versionUtil: VersionUtil,
) {
    @CheckResult
    operator fun invoke(): Boolean {
        val lastSeenVersion = deviceLocalPreferences.changeLogLastVersion
        if (isPermanentlyHidden) {
            return false
        }
        if (!WebViewChecker.isWebViewAvailable()) {
            return false
        }
        val version = versionUtil.getVersionName()
        deviceLocalPreferences.changeLogLastVersion = version
        return lastSeenVersion != null && version != lastSeenVersion
    }

    private val isPermanentlyHidden: Boolean
        get() = deviceLocalPreferences.isChangeLogPermanentlyHidden
}
