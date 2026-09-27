package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.runtime.Stable
import androidx.core.net.toUri
import ch.rmy.android.framework.extensions.runIf
import javax.inject.Inject

class AvailableBrowserPackageNamesLookup
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

    @Stable
    data class InstalledBrowser(
        val packageName: String,
        val appName: String? = null,
    )
}
