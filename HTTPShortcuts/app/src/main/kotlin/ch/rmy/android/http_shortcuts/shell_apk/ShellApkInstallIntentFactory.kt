package ch.rmy.android.http_shortcuts.shell_apk

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.net.toUri
import java.io.File
import javax.inject.Inject

class ShellApkInstallIntentFactory
@Inject
constructor(
    private val context: Context,
) {

    fun canRequestPackageInstalls(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
            context.packageManager.canRequestPackageInstalls()

    fun createInstallIntent(apkFile: File): Intent =
        ShellApkInstallerActivity.createIntent(context, apkFile)

    fun createManageUnknownSourcesIntent(): Intent =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                "package:${context.packageName}".toUri(),
            )
        } else {
            Intent(Settings.ACTION_SECURITY_SETTINGS)
        }
}
