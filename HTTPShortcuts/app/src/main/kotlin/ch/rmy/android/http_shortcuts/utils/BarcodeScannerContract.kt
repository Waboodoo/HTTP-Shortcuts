package ch.rmy.android.http_shortcuts.utils

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

object BarcodeScannerContract : ActivityResultContract<Unit, String?>() {
    override fun createIntent(context: Context, input: Unit): Intent =
        Intent("la.droid.qr.scan")
            .putExtra("la.droid.qr.complete", true)

    override fun parseResult(resultCode: Int, intent: Intent?): String? =
        intent
            ?.takeIf { resultCode == RESULT_OK }
            ?.getStringExtra("la.droid.qr.result")
}
