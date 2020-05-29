package ch.rmy.android.http_shortcuts.logging

import android.content.Context
import android.util.Log
import ch.rmy.android.http_shortcuts.BuildConfig
import ch.rmy.android.http_shortcuts.extensions.consume
import ch.rmy.android.http_shortcuts.utils.InstallUtil
import ch.rmy.android.http_shortcuts.utils.Settings
import com.bugsnag.android.Bugsnag
import java.util.Date

object Logging {

    /**
     * Disable crash logging after 3 months to prevent old bugs from spamming
     */
    private const val MAX_APP_AGE = 3 * 30 * 24 * 60 * 60 * 1000L

    private var initialized = false

    fun initCrashReporting(context: Context) {
        if (isAppOutdated || !Settings(context).isCrashReportingAllowed) {
            return
        }

        if (BuildConfig.BUGSNAG_API_KEY.isEmpty()) {
            throw IllegalStateException("Bugsnag API key not set")
        }

        Bugsnag.init(context, BuildConfig.BUGSNAG_API_KEY)
        Bugsnag.setUserId(Settings(context).userId)
        Bugsnag.beforeNotify { error ->
            consume {
                error.addToTab("app", "installedFromStore", InstallUtil.isAppInstalledFromStore(context))
            }
        }
        initialized = true
    }

    private val isAppOutdated
        get() = Date().time - BuildConfig.BUILD_TIMESTAMP.toLong() > MAX_APP_AGE

    val supportsCrashReporting: Boolean = true

    fun disableCrashReporting() {
        if (initialized) {
            Bugsnag.disableExceptionHandler()
        }
    }

    fun logException(origin: String, e: Throwable) {
        if (initialized) {
            Bugsnag.notify(e)
        }
    }

    fun logInfo(origin: String, message: String) {
        if (initialized) {
            Bugsnag.leaveBreadcrumb(message)
        }
    }

}
