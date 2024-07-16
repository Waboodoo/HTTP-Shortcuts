package ch.rmy.android.http_shortcuts.logging

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.http_shortcuts.BuildConfig

@SuppressLint("StaticFieldLeak")
object Logging : ch.rmy.android.framework.extensions.Logging {

    private const val TAG = "Logging"

    private var context: Context? = null

    fun initCrashReporting(context: Context) {
        if (BuildConfig.DEBUG) {
            this.context = context
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun disableCrashReporting(context: Context) {
    }

    @Suppress("MayBeConstant")
    val supportsCrashReporting: Boolean = false

    override fun logException(origin: String?, e: Throwable) {
        if (BuildConfig.DEBUG) {
            Log.e(origin ?: TAG, "An error occurred", e)
            e.printStackTrace()
            Handler(Looper.getMainLooper()).post {
                context?.showToast("Error: $e", long = true)
            }
        }
    }

    override fun logInfo(origin: String?, message: String) {
        if (BuildConfig.DEBUG) {
            Log.i(origin ?: TAG, message)
        }
    }
}
