package ch.rmy.android.http_shortcuts.utils

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract

object PickFileContract : ActivityResultContract<Unit?, (ContentResolver) -> Uri?>() {
    override fun createIntent(context: Context, input: Unit?): Intent =
        Intent(Intent.ACTION_OPEN_DOCUMENT)
            .setType("*/*")
            .addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
                    or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                    or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION,
            )

    override fun parseResult(resultCode: Int, intent: Intent?): (ContentResolver) -> Uri? =
        { contentResolver ->
            intent?.data
                ?.also {
                    contentResolver.takePersistableUriPermission(
                        it,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION,
                    )
                }
        }
}
