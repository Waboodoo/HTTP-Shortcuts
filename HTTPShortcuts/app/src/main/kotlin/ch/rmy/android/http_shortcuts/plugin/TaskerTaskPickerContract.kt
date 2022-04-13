package ch.rmy.android.http_shortcuts.plugin

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

object TaskerTaskPickerContract : ActivityResultContract<Unit, String?>() {
    override fun createIntent(context: Context, input: Unit): Intent =
        TaskerIntent.getTaskSelectIntent()

    override fun parseResult(resultCode: Int, intent: Intent?): String? =
        intent?.dataString
}
