package ch.rmy.android.http_shortcuts.extensions

import android.util.Log
import ch.rmy.android.http_shortcuts.BuildConfig
import ch.rmy.android.http_shortcuts.utils.CrashReporting

fun Any.logException(e: Throwable) {
    CrashReporting.logException(this.javaClass.simpleName, e)
}

fun Any.logInfo(message: String) {
    CrashReporting.logInfo(this.javaClass.simpleName, message)
}

fun <T> Any.tryOrLog(block: () -> T): T? =
    try {
        block()
    } catch (e: Throwable) {
        logException(e)
        null
    }

fun <T> Any.tryOrIgnore(block: () -> T): T? =
    try {
        block()
    } catch (e: Throwable) {
        if (BuildConfig.DEBUG) {
            Log.e(this.javaClass.simpleName, "An ignorable error occurred", e)
        }
        null
    }