package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.pm.PackageInfoCompat

object VersionUtil {
    fun getVersion(context: Context): Long =
        try {
            (PackageInfoCompat.getLongVersionCode(getPackageInfo(context)) / 10000) - 110000
        } catch (e: PackageManager.NameNotFoundException) {
            0L
        }

    fun getVersionName(context: Context): String =
        try {
            getPackageInfo(context).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            "???"
        }

    private fun getPackageInfo(context: Context) =
        context.packageManager.getPackageInfo(context.packageName, 0)
}
