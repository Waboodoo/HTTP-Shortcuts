package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import ch.rmy.android.framework.extensions.getParcelable

object RingtonePickerContract : ActivityResultContract<Unit, Uri?>() {
    override fun createIntent(context: Context, input: Unit): Intent =
        Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
            .putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? =
        intent?.getParcelable(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
}
