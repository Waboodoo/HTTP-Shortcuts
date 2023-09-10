package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.getSystemService
import javax.inject.Inject

class VibrationUtil
@Inject
constructor(
    private val context: Context,
) {

    fun getVibrator(): Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService<VibratorManager>()
                ?.defaultVibrator
        } else {
            context.getSystemService()
        }
            ?.takeIf { it.hasVibrator() }

    fun canVibrate(): Boolean =
        getVibrator() != null
}
