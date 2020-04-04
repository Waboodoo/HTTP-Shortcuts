package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.util.Log
import ch.rmy.android.http_shortcuts.BuildConfig
import ch.rmy.android.http_shortcuts.extensions.consume
import com.bugsnag.android.Bugsnag
import kotlin.properties.Delegates

object CrashReporting {

    private var initialized = false

    fun init(context: Context) {
        if (BuildConfig.BUGSNAG_API_KEY.isEmpty() || BuildConfig.DEBUG) {
            return
        }
        Bugsnag.init(context, BuildConfig.BUGSNAG_API_KEY)
        Bugsnag.beforeNotify { error ->
            consume {
                error.addToTab("app", "installedFromStore", InstallUtil.isAppInstalledFromStore(context))
            }
        }
        initialized = true
    }

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
