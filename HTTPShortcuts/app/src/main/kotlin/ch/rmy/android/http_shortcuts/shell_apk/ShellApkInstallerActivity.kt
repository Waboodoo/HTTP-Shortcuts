package ch.rmy.android.http_shortcuts.shell_apk

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.http_shortcuts.R
import java.io.File

class ShellApkInstallerActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            if (intent.action == ACTION_INSTALL_STATUS) {
                handleInstallStatus(intent)
                return
            }

            val apkFile = File(intent.getStringExtra(EXTRA_APK_PATH).orEmpty())
            if (!apkFile.isFile) {
                showToast(R.string.error_shell_apk_invalid, long = true)
                finish()
                return
            }

            val packageName = getArchivePackageName(apkFile)
            if (packageName == null) {
                showToast(R.string.error_shell_apk_invalid, long = true)
                finish()
                return
            }

            install(apkFile, packageName)
            deleteTemporaryApkFiles(apkFile)
            showToast(R.string.message_shell_apk_install_started)
        } catch (e: Exception) {
            logException(e)
            showToast(getString(R.string.error_shell_apk_install_failed, e.message.orEmpty()), long = true)
        } finally {
            finish()
        }
    }

    private fun handleInstallStatus(intent: Intent) {
        when (val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                intent.getConfirmationIntent()
                    ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    ?.let(::startActivity)
                    ?: showInstallFailure("status=$status")
            }
            PackageInstaller.STATUS_SUCCESS -> {
                showToast(R.string.message_shell_apk_installed)
            }
            else -> {
                // Some vendor installers report STATUS_FAILURE_ABORTED after the package has already been installed.
                // Trust the package manager over the callback before showing a failure to the user.
                val expectedPackageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
                if (expectedPackageName != null && isPackageInstalled(expectedPackageName)) {
                    showToast(R.string.message_shell_apk_installed)
                } else {
                    val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                        .orEmpty()
                        .ifEmpty { "status=$status" }
                    showInstallFailure("status=$status, $message")
                }
            }
        }
    }

    private fun deleteTemporaryApkFiles(apkFile: File) {
        runCatching {
            // PackageInstaller has copied the APK into its session after commit(), so the cache copy can be removed.
            apkFile.parentFile?.deleteRecursively()
        }.onFailure(::logException)
    }

    private fun install(apkFile: File, packageName: String) {
        val packageInstaller = packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
            .apply {
                setAppPackageName(packageName)
                setSize(apkFile.length())
            }
        val sessionId = packageInstaller.createSession(params)
        var session: PackageInstaller.Session? = null
        try {
            session = packageInstaller.openSession(sessionId)
            apkFile.inputStream().use { input ->
                session.openWrite(APK_SESSION_NAME, 0, apkFile.length()).use { output ->
                    input.copyTo(output)
                    session.fsync(output)
                }
            }
            session.commit(createStatusPendingIntent(sessionId, packageName).intentSender)
        } catch (e: Exception) {
            packageInstaller.abandonSession(sessionId)
            throw e
        } finally {
            session?.close()
        }
    }

    @Suppress("DEPRECATION")
    private fun getArchivePackageName(apkFile: File): String? =
        packageManager.getPackageArchiveInfo(apkFile.absolutePath, 0)
            ?.packageName

    private fun isPackageInstalled(packageName: String): Boolean =
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0)
            }
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }

    private fun createStatusPendingIntent(sessionId: Int, packageName: String): PendingIntent =
        PendingIntent.getActivity(
            this,
            sessionId,
            Intent(this, ShellApkInstallerActivity::class.java)
                .setAction(ACTION_INSTALL_STATUS)
                .putExtra(EXTRA_PACKAGE_NAME, packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0,
        )

    @Suppress("DEPRECATION")
    private fun Intent.getConfirmationIntent(): Intent? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
        } else {
            getParcelableExtra(Intent.EXTRA_INTENT)
        }

    private fun showInstallFailure(message: String) {
        showToast(
            getString(R.string.error_shell_apk_install_failed, message.ifEmpty { "unknown" }),
            long = true,
        )
    }

    companion object {
        private const val ACTION_INSTALL_STATUS = "ch.rmy.android.http_shortcuts.shell_apk.INSTALL_STATUS"
        private const val APK_SESSION_NAME = "base.apk"
        private const val EXTRA_APK_PATH = "apk_path"
        private const val EXTRA_PACKAGE_NAME = "package_name"

        fun createIntent(context: Context, apkFile: File): Intent =
            Intent(context, ShellApkInstallerActivity::class.java)
                .putExtra(EXTRA_APK_PATH, apkFile.absolutePath)
    }
}
