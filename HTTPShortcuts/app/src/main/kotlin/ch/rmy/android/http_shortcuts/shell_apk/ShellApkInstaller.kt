package ch.rmy.android.http_shortcuts.shell_apk

import android.content.Intent
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import javax.inject.Inject

class ShellApkInstaller
@Inject
constructor(
    private val packageNameFactory: ShellApkPackageNameFactory,
    private val shellApkBuilder: ShellApkBuilder,
    private val installIntentFactory: ShellApkInstallIntentFactory,
) {

    suspend fun prepareInstall(
        shortcutId: ShortcutId,
        shortcutName: String,
        shortcutIcon: ShortcutIcon,
    ): Result {
        if (!installIntentFactory.canRequestPackageInstalls()) {
            return Result.PermissionRequired(installIntentFactory.createManageUnknownSourcesIntent())
        }
        val packageName = packageNameFactory.createPackageName(shortcutId)
        val apkFile = shellApkBuilder.build(
            shortcutId = shortcutId,
            packageName = packageName,
            appName = shortcutName,
            icon = shortcutIcon,
        )
        return Result.ReadyToInstall(installIntentFactory.createInstallIntent(apkFile))
    }

    sealed interface Result {
        data class ReadyToInstall(val intent: Intent) : Result
        data class PermissionRequired(val intent: Intent) : Result
    }
}
