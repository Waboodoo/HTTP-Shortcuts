package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

object VersionUtil {
    fun getVersionName(context: Context): String =
        try {
            getPackageInfo(context).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            "???"
        }

    @Suppress("DEPRECATION")
    private fun getPackageInfo(context: Context) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            context.packageManager.getPackageInfo(context.packageName, 0)
        }
}
