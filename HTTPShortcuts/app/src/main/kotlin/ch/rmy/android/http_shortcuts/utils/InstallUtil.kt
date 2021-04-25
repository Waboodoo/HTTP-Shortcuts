package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.os.Build
import ch.rmy.android.http_shortcuts.extensions.tryOrLog

object InstallUtil {

    @Suppress("unused")
    fun isAppInstalledFromPlayStore(context: Context): Boolean =
        (getInstallerPackageName(context) ?: "") in PLAY_STORE_PACKAGES

    private fun getInstallerPackageName(context: Context): String? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            tryOrLog {
                context.packageManager.getInstallSourceInfo(context.packageName)
                    .initiatingPackageName
            }
        } else {
            context.packageManager.getInstallerPackageName(context.packageName)
        }

    private val PLAY_STORE_PACKAGES = setOf(
        "com.android.vending",
        "com.google.android.feedback",
    )

}
