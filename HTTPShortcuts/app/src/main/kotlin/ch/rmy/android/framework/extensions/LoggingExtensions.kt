package ch.rmy.android.framework.extensions

import android.util.Log
import ch.rmy.android.http_shortcuts.BuildConfig
import ch.rmy.android.http_shortcuts.logging.Logging

fun Any.logException(e: Throwable) {
    Logging.logException(this.javaClass.simpleName.ifEmpty { "anonymous" }, e)
}

fun Any.logInfo(message: String) {
    Logging.logInfo(this.javaClass.simpleName.ifEmpty { "anonymous" }, message)
}

inline fun <T> Any.tryOrLog(block: () -> T): T? =
    try {
        block()
    } catch (e: Throwable) {
        logException(e)
        null
    }

inline fun <T> Any.tryOrIgnore(block: () -> T): T? =
    try {
        block()
    } catch (e: Throwable) {
        if (BuildConfig.DEBUG) {
            Log.e(this.javaClass.simpleName, "An ignorable error occurred", e)
        }
        null
    }
