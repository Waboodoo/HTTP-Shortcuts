package ch.rmy.android.framework.utils

import android.content.Context
import android.os.Build
import ch.rmy.android.framework.extensions.tryOrLog
import javax.inject.Inject

class InstallUtil
@Inject
constructor(
    private val context: Context,
) {
    fun isAppInstalledFromPlayStore(): Boolean =
        (getInstallerPackageName() ?: "") in PLAY_STORE_PACKAGES

    private fun getInstallerPackageName(): String? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            tryOrLog {
                context.packageManager.getInstallSourceInfo(context.packageName)
                    .initiatingPackageName
            }
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getInstallerPackageName(context.packageName)
        }

    companion object {
        private val PLAY_STORE_PACKAGES = setOf(
            "com.android.vending",
            "com.google.android.feedback",
        )
    }
}
