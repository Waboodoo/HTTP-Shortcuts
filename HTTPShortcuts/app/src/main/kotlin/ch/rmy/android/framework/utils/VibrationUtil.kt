package ch.rmy.android.framework.utils

import android.content.Context
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager

class VibrationUtil(private val context: Context) {

    fun getVibrator(): Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibrationManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibrationManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
            .takeIf { it.hasVibrator() }

    fun canVibrate(): Boolean =
        getVibrator() != null
}
