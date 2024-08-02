package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.net.toUri
import javax.inject.Inject

class AppOverlayUtil
@Inject
constructor(
    private val context: Context,
) {
    fun getSettingsIntent(): Intent =
        Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            .setData("package:${context.packageName}".toUri())

    fun canDrawOverlays() =
        Settings.canDrawOverlays(context)
}
