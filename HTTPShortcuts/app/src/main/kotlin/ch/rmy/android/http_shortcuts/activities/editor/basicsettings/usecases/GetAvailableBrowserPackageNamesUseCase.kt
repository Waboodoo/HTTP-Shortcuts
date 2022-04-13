package ch.rmy.android.http_shortcuts.activities.editor.basicsettings.usecases

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.net.toUri
import ch.rmy.android.framework.extensions.mapIf

class GetAvailableBrowserPackageNamesUseCase(
    private val context: Context,
) {

    operator fun invoke(currentValue: String): List<String> =
        context.packageManager.queryIntentActivities(
            Intent(Intent.ACTION_VIEW, "https://http-shortcuts.rmy.ch".toUri()),
            PackageManager.MATCH_ALL,
        )
            .map {
                it.activityInfo.packageName
            }
            .let { browserPackageNames ->
                browserPackageNames.mapIf(currentValue.isNotEmpty() && currentValue !in browserPackageNames) {
                    plus(currentValue)
                }
            }
            .sorted()
}
