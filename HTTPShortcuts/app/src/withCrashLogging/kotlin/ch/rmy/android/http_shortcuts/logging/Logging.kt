package ch.rmy.android.http_shortcuts.logging

import android.content.Context
import android.view.InflateException
import ch.rmy.android.framework.utils.InstallUtil
import ch.rmy.android.http_shortcuts.BuildConfig
import ch.rmy.android.http_shortcuts.data.RealmFactory
import ch.rmy.android.http_shortcuts.utils.Settings
import com.bugsnag.android.Bugsnag
import com.bugsnag.android.Configuration
import com.bugsnag.android.ErrorTypes
import com.bugsnag.android.ThreadSendPolicy
import kotlinx.coroutines.CancellationException
import java.io.IOException
import java.util.Date
import kotlin.time.Duration.Companion.days
import kotlin.time.times

object Logging : ch.rmy.android.framework.extensions.Logging {

    /**
     * Disable crash logging after 3 months to prevent old bugs from spamming
     */
    private val MAX_APP_AGE = 3 * 30.days

    private var initialized = false

    fun initCrashReporting(context: Context) {
        if (isAppOutdated || !Settings(context).isCrashReportingAllowed) {
            return
        }

        if (BuildConfig.BUGSNAG_API_KEY.isEmpty()) {
            error("Bugsnag API key not set")
        }

        Bugsnag.start(context, createBugsnagConfig())
        Bugsnag.setUser(Settings(context).userId, null, null)
        Bugsnag.addOnError { event ->
            event.addMetadata("app", "installedFromStore", InstallUtil.isAppInstalledFromPlayStore(context))
            event.originalError?.let { !shouldIgnore(it) } ?: true
        }
        initialized = true
    }

    private fun createBugsnagConfig() =
        Configuration(BuildConfig.BUGSNAG_API_KEY)
            .apply {
                sendThreads = ThreadSendPolicy.NEVER
                autoTrackSessions = false
                enabledErrorTypes = ErrorTypes(anrs = false, ndkCrashes = false)
            }

    private val isAppOutdated
        get() = Date().time - BuildConfig.BUILD_TIMESTAMP.toLong() > MAX_APP_AGE.inWholeMilliseconds

    @Suppress("MayBeConstant")
    val supportsCrashReporting: Boolean = true

    fun disableCrashReporting(context: Context) {
        if (initialized) {
            Configuration.load(context).apply {
                enabledErrorTypes = ErrorTypes(
                    anrs = false,
                    ndkCrashes = false,
                    unhandledExceptions = false,
                    unhandledRejections = false,
                )
            }
        }
    }

    override fun logException(origin: String, e: Throwable) {
        if (initialized && !shouldIgnore(e)) {
            Bugsnag.leaveBreadcrumb("Logging exception from $origin")
            Bugsnag.notify(e)
        }
    }

    private fun shouldIgnore(e: Throwable) =
        e is IOException ||
            e.cause is IOException ||
            e is CancellationException ||
            e is InflateException ||
            e is RealmFactory.RealmNotFoundException ||
            e.stackTrace.any { it.className.contains("Miui") }

    override fun logInfo(origin: String, message: String) {
        if (initialized) {
            Bugsnag.leaveBreadcrumb("$origin: $message")
        }
    }
}
