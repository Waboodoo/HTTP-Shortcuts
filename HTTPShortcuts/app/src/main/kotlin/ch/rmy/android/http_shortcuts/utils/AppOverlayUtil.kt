package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.net.toUri
import javax.inject.Inject

class AppOverlayUtil
@Inject
constructor(
    private val context: Context,
) {

    fun getSettingsIntent(): Intent? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                .setData("package:${context.packageName}".toUri())
        } else null

    fun canDrawOverlays() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
}
