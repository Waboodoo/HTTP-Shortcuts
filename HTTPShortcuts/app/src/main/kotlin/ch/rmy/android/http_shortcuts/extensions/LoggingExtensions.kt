package ch.rmy.android.http_shortcuts.extensions

import android.util.Log
import ch.rmy.android.http_shortcuts.utils.CrashReporting

fun Any.logException(e: Throwable) {
    if (CrashReporting.enabled) {
        CrashReporting.logException(e)
    } else {
        Log.e(this.javaClass.simpleName, "An error occurred", e)
    }
}

fun Any.tryOrLog(block: () -> Unit) {
    try {
        block()
    } catch (e: Throwable) {
        logException(e)
    }
}