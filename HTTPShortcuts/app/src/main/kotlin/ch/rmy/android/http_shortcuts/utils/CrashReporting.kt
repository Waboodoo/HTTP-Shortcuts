package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.util.Log
import ch.rmy.android.http_shortcuts.BuildConfig
import ch.rmy.android.http_shortcuts.extensions.consume
import com.bugsnag.android.Bugsnag
import java.util.Date
import kotlin.properties.Delegates

object CrashReporting {

    /**
     * Disable crash logging after 3 months to prevent old bugs from spamming
     */
    private const val MAX_APP_AGE = 3 * 30 * 24 * 60 * 60 * 1000L

    private var initialized = false

    fun init(context: Context) {
        if (BuildConfig.BUGSNAG_API_KEY.isEmpty() || BuildConfig.DEBUG || isAppOutdated) {
            return
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

    var enabled: Boolean by Delegates.observable(true) { _, old, new ->
        if (initialized && old != new) {
            if (new) {
                Bugsnag.enableExceptionHandler()
            } else {
                Bugsnag.disableExceptionHandler()
            }
        }
    }

    fun logException(e: Throwable) {
        if (initialized) {
            Bugsnag.notify(e)
        } else {
            Log.e("CrashReporting", "An error occurred", e)
            e.printStackTrace()
        }
    }

}
