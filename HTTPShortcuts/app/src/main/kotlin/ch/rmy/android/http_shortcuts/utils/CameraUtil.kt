package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.content.pm.PackageManager
import javax.inject.Inject

class CameraUtil
@Inject
constructor(
    private val context: Context,
) {
    fun hasCamera(): Boolean =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
}
