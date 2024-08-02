package ch.rmy.android.http_shortcuts.activities.editor.basicsettings.usecases

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.net.toUri
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.http_shortcuts.activities.editor.basicsettings.models.InstalledBrowser
import javax.inject.Inject

class GetAvailableBrowserPackageNamesUseCase
@Inject
constructor(
    private val context: Context,
) {
    operator fun invoke(currentValue: String?): List<InstalledBrowser> =
        context.packageManager.queryIntentActivities(
            Intent(Intent.ACTION_VIEW, "https://http-shortcuts.rmy.ch".toUri()),
            PackageManager.MATCH_ALL,
        )
            .map {
                InstalledBrowser(
                    packageName = it.activityInfo.packageName,
                    appName = it.activityInfo.applicationInfo.loadLabel(context.packageManager).toString(),
                )
            }
            .let { browsers ->
                browsers.runIf(currentValue != null && browsers.none { it.packageName == currentValue }) {
                    plus(InstalledBrowser(packageName = currentValue!!))
                }
            }
            .sortedBy { it.appName ?: it.packageName }
}
