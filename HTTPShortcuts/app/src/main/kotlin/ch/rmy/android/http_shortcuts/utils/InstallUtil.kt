package ch.rmy.android.http_shortcuts.utils

import android.content.Context

object InstallUtil {

    /**
     * Checks if the app was installed from a store app.
     */
    fun isAppInstalledFromStore(context: Context): Boolean =
        (context.packageManager.getInstallerPackageName(context.packageName) ?: "") in STORE_PACKAGES

    private val STORE_PACKAGES = setOf(
        "com.android.vending",
        "com.google.android.feedback"
    )
}
