package ch.rmy.android.http_shortcuts.logging

import android.content.Context
import android.view.InflateException
import ch.rmy.android.framework.extensions.minus
import ch.rmy.android.framework.utils.InstallUtil
import ch.rmy.android.http_shortcuts.BuildConfig
import ch.rmy.android.http_shortcuts.utils.Settings
import com.bugsnag.android.Bugsnag
import com.bugsnag.android.Configuration
import com.bugsnag.android.ErrorTypes
import com.bugsnag.android.ThreadSendPolicy
import kotlinx.coroutines.CancellationException
import java.io.IOException
import java.time.Instant
import kotlin.time.Duration.Companion.days
import kotlin.time.times

object Logging : ch.rmy.android.framework.extensions.Logging {

    /**
     * Disable crash logging after 3 months to prevent old bugs from spamming
     */
    private val MAX_APP_AGE = 3 * 30.days

    /**
     * List of devices who somehow manage to produce cryptic errors that I'm unable to debug or understand, and a lot of them too.
     * Suppressing them in a desperate attempt at avoiding going over error logging quota all the time.
     */
    private val BLACKLISTED_DEVICE_IDS = listOf(
        "853b9ed3-f3ed-4136-8af7-0ff02d333ae3",
        "c5080f5e-823b-4d47-868e-8711e2841961",
        "58087851-ef5b-4fa6-822b-9d4d4b5081af",
    )

    private var initialized = false

    fun initCrashReporting(context: Context) {
        val settings = Settings(context)
        val deviceId = settings.deviceId
        if (isAppOutdated || !settings.isCrashReportingAllowed || deviceId in BLACKLISTED_DEVICE_IDS) {
            return
        }

        if (BuildConfig.BUGSNAG_API_KEY.isEmpty()) {
            error("Bugsnag API key not set")
        }

        Bugsnag.start(context, createBugsnagConfig())
        Bugsnag.setUser(deviceId, null, null)
        Bugsnag.addOnError { event ->
            event.addMetadata("app", "installedFromStore", InstallUtil(context).isAppInstalledFromPlayStore())
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
        get() = Instant.now() - Instant.ofEpochMilli(BuildConfig.BUILD_TIMESTAMP.toLong()) > MAX_APP_AGE

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

    override fun logException(origin: String?, e: Throwable) {
        if (initialized && !shouldIgnore(e)) {
            Bugsnag.notify(e)
        }
    }

    private fun shouldIgnore(e: Throwable) =
        e is IOException ||
            e.cause is IOException ||
            e is CancellationException ||
            e is InflateException ||
            e.stackTrace.any { it.className.contains("Miui") }

    override fun logInfo(origin: String?, message: String) {
        if (initialized) {
            Bugsnag.leaveBreadcrumb("${origin?.plus(": ") ?: ""}$message")
        }
    }
}
