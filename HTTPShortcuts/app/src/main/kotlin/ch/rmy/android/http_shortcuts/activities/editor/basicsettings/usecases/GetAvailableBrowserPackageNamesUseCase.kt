package ch.rmy.android.http_shortcuts.activities.editor.basicsettings.usecases

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.net.toUri

class GetAvailableBrowserPackageNamesUseCase(
    private val context: Context,
) {

    operator fun invoke(): List<String> =
        context.packageManager.queryIntentActivities(
            Intent(Intent.ACTION_VIEW, "https://http-shortcuts.rmy.ch".toUri()),
            PackageManager.MATCH_ALL,
        )
            .map {
                it.activityInfo.packageName
            }
}
