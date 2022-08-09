package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import javax.inject.Inject

class VersionUtil
@Inject
constructor(
    private val context: Context,
) {
    fun getVersionName(): String =
        try {
            getPackageInfo(context).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            "???"
        }

    fun getVersionCode(): Long =
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                getPackageInfo(context).longVersionCode
            } else {
                getPackageInfo(context).versionCode.toLong()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            -1
        }

    @Suppress("DEPRECATION")
    private fun getPackageInfo(context: Context) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            context.packageManager.getPackageInfo(context.packageName, 0)
        }
}
