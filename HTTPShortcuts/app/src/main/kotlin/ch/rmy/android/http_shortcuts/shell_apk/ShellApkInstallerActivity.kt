package ch.rmy.android.http_shortcuts.shell_apk

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import ch.rmy.android.framework.extensions.getParcelable
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.extensions.tryOrLog
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.settings.DeviceLocalPreferences
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class ShellApkInstallerActivity : AppCompatActivity() {

    @Inject
    lateinit var deviceLocalPreferences: DeviceLocalPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            if (intent.isShellApkInstallStatusIntent()) {
                handleInstallStatus(intent)
                return
            }

            val apkFile = intent.getStringExtra(EXTRA_APK_PATH)?.let(::File)
            if (apkFile == null || !apkFile.isFile) {
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
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        logInfo("Handling install status $status")
        when (status) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val confirmationIntent = intent.getParcelable<Intent>(Intent.EXTRA_INTENT)
                if (confirmationIntent == null) {
                    showInstallFailure("intent was unexpected null")
                    logException(RuntimeException("Confirmation intent was null"))
                    return
                }
                confirmationIntent
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .startActivity(this)
            }
            PackageInstaller.STATUS_SUCCESS -> {
                showToast(R.string.message_shell_apk_installed)
            }
            PackageInstaller.STATUS_FAILURE_ABORTED,
            PackageInstaller.STATUS_FAILURE_TIMEOUT,
            -> Unit
            else -> {
                // Some vendor installers report STATUS_FAILURE_ABORTED after the package has already been installed.
                // Trust the package manager over the callback before showing a failure to the user.
                val expectedPackageNames = intent.getShellApkExpectedPackageNames()
                    .runIfNotNull(deviceLocalPreferences.pendingShellApkInstallation) { pendingPackageName ->
                        plus(pendingPackageName)
                    }
                deviceLocalPreferences.pendingShellApkInstallation = null
                if (expectedPackageNames.any(::isPackageInstalled)) {
                    showToast(R.string.message_shell_apk_installed)
                } else {
                    val statusMessage = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)?.takeUnlessEmpty()
                    showInstallFailure(
                        message = statusMessage ?: "status=$status",
                    )
                    logInfo("Installation failed: $status, $statusMessage")
                    logException(RuntimeException("APK installation failed"))
                }
            }
        }
    }

    private fun deleteTemporaryApkFiles(apkFile: File) {
        tryOrLog {
            apkFile.parentFile?.deleteRecursively()
        }
    }

    private fun install(apkFile: File, packageName: String) {
        logInfo("Installing APK")
        val packageInstaller = packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
            .apply {
                setAppPackageName(packageName)
                setSize(apkFile.length())
            }
        val sessionId = packageInstaller.createSession(params)
        var session: PackageInstaller.Session? = null
        try {
            deviceLocalPreferences.pendingShellApkInstallation = packageName
            session = packageInstaller.openSession(sessionId)
            apkFile.inputStream().use { input ->
                session.openWrite(APK_SESSION_NAME, 0, apkFile.length()).use { output ->
                    input.copyTo(output)
                    session.fsync(output)
                }
            }
            session.commit(createStatusPendingIntent(sessionId, packageName).intentSender)
        } catch (e: Exception) {
            deviceLocalPreferences.pendingShellApkInstallation = null
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
            createStatusIntent(this, packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0,
        )

    private fun showInstallFailure(message: String) {
        showToast(getString(R.string.error_shell_apk_install_failed, message), long = true)
    }

    companion object {
        private const val APK_SESSION_NAME = "base.apk"
        private const val EXTRA_APK_PATH = "apk_path"
        private const val EXTRA_SHELL_APK_PACKAGE_NAME = "package_name"
        private const val SHELL_APK_INSTALL_STATUS_ACTION_PREFIX = "ch.rmy.android.http_shortcuts.shell_apk.INSTALL_STATUS."

        private fun Intent.isShellApkInstallStatusIntent(): Boolean =
            action?.startsWith(SHELL_APK_INSTALL_STATUS_ACTION_PREFIX) == true ||
                hasExtra(PackageInstaller.EXTRA_STATUS)

        private fun Intent.getShellApkExpectedPackageNames(): Set<String> =
            listOfNotNull(
                getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME),
                getStringExtra(EXTRA_SHELL_APK_PACKAGE_NAME),
                data?.schemeSpecificPart,
                action
                    ?.takeIf { it.startsWith(SHELL_APK_INSTALL_STATUS_ACTION_PREFIX) }
                    ?.removePrefix(SHELL_APK_INSTALL_STATUS_ACTION_PREFIX),
            )
                .filter(String::isNotEmpty)
                .toSet()

        fun createInstallIntent(context: Context, apkFile: File): Intent =
            Intent(context, ShellApkInstallerActivity::class.java)
                .putExtra(EXTRA_APK_PATH, apkFile.absolutePath)

        fun createStatusIntent(context: Context, packageName: String): Intent =
            Intent(context, ShellApkInstallerActivity::class.java)
                // The action survives vendor PackageInstaller fill-in intents more reliably than extras or data.
                .setAction("$SHELL_APK_INSTALL_STATUS_ACTION_PREFIX$packageName")
                // Some vendor installers drop custom extras from the final status callback.
                .setData("http-shortcuts-install:$packageName".toUri())
                .putExtra(PackageInstaller.EXTRA_PACKAGE_NAME, packageName)
                .putExtra(EXTRA_SHELL_APK_PACKAGE_NAME, packageName)
    }
}
