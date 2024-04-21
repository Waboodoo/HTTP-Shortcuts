package ch.rmy.android.http_shortcuts.activities.main.usecases

import android.webkit.WebView
import androidx.annotation.CheckResult
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.http_shortcuts.utils.Settings
import ch.rmy.android.http_shortcuts.utils.VersionUtil
import javax.inject.Inject

class ShouldShowChangeLogDialogUseCase
@Inject
constructor(
    private val settings: Settings,
    private val versionUtil: VersionUtil,
) {
    @CheckResult
    operator fun invoke(): Boolean {
        val lastSeenVersion = settings.changeLogLastVersion
        if (isPermanentlyHidden) {
            return false
        }
        try {
            // Try to access a harmless method on the WebView. This will fail if no WebView is installed.
            WebView.setWebContentsDebuggingEnabled(false)
        } catch (e: Exception) {
            logException(e)
            return false
        }
        val version = versionUtil.getVersionName()
        settings.changeLogLastVersion = version
        return lastSeenVersion != null && version != lastSeenVersion
    }

    private val isPermanentlyHidden: Boolean
        get() = settings.isChangeLogPermanentlyHidden
}
