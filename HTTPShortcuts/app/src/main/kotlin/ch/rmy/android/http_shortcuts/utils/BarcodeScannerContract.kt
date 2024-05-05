package ch.rmy.android.http_shortcuts.utils

import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

object BarcodeScannerContract : ActivityResultContract<Unit, String?>() {

    override fun createIntent(context: Context, input: Unit): Intent {
        for (targetApp in BarcodeScannerApp.entries) {
            val intent = targetApp.createIntent()
            if (context.packageManager.queryIntentActivities(intent, 0).isNotEmpty()) {
                return intent
            }
        }
        throw ActivityNotFoundException()
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String? =
        intent
            ?.takeIf { resultCode == RESULT_OK }
            ?.run {
                for (targetApp in BarcodeScannerApp.entries) {
                    val result = targetApp.getResult(this)
                    if (result != null) {
                        return@run result
                    }
                }
                null
            }

    private enum class BarcodeScannerApp(
        val createIntent: () -> Intent,
        val getResult: Intent.() -> String?,
    ) {
        BINARY_EYE(
            createIntent = {
                Intent("com.google.zxing.client.android.SCAN")
                    .setPackage("de.markusfisch.android.binaryeye")
            },
            getResult = {
                getStringExtra("SCAN_RESULT")
            },
        ),
        ZXING(
            createIntent = {
                Intent("com.google.zxing.client.android.SCAN")
                    .setPackage("com.google.zxing.client.android")
            },
            getResult = {
                getStringExtra("SCAN_RESULT")
            },
        ),
        QR_DROID(
            createIntent = {
                Intent("la.droid.qr.scan")
                    .setPackage("la.droid.qr")
                    .putExtra("la.droid.qr.complete", true)
            },
            getResult = {
                getStringExtra("la.droid.qr.result")
            },
        ),
    }
}
