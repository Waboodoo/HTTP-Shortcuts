package ch.rmy.android.http_shortcuts.shell_apk

import android.content.Context
import android.content.Intent
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
        context.packageManager.canRequestPackageInstalls()

    fun createInstallIntent(apkFile: File): Intent =
        ShellApkInstallerActivity.createInstallIntent(context, apkFile)

    fun createManageUnknownSourcesIntent(): Intent =
        Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            "package:${context.packageName}".toUri(),
        )
}
