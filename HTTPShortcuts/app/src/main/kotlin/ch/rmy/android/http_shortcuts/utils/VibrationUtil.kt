package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager

object VibrationUtil {

    fun getVibrator(context: Context): Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibrationManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibrationManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
            .takeIf { it.hasVibrator() }

    fun canVibrate(context: Context): Boolean =
        getVibrator(context) != null

}