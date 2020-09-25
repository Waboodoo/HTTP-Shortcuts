package ch.rmy.android.http_shortcuts.logging

import android.content.Context
import android.util.Log
import ch.rmy.android.http_shortcuts.BuildConfig

object Logging {

    fun initCrashReporting(context: Context) {}

    fun disableCrashReporting(context: Context) {}

    val supportsCrashReporting: Boolean = false

    fun logException(origin: String, e: Throwable) {
        if (BuildConfig.DEBUG) {
            Log.e(origin, "An error occurred", e)
            e.printStackTrace()
        }
    }

    fun logInfo(origin: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.i(origin, message)
        }
    }

}
