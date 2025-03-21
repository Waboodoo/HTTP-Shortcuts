package ch.rmy.android.http_shortcuts.utils

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.result.contract.ActivityResultContract
import androidx.documentfile.provider.DocumentFile
import ch.rmy.android.framework.extensions.runIfNotNull

object PickDirectoryContract : ActivityResultContract<Uri?, (ContentResolver) -> Uri?>() {
    override fun createIntent(context: Context, input: Uri?): Intent =
        Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            .addCategory(Intent.CATEGORY_DEFAULT)
            .runIfNotNull(input) {
                runIfNotNull(
                    try {
                        DocumentFile.fromTreeUri(context, it)?.uri
                    } catch (_: IllegalArgumentException) {
                        null
                    },
                ) { fileUri ->
                    putExtra(DocumentsContract.EXTRA_INITIAL_URI, fileUri)
                }
            }
            .addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
                    or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                    or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION,
            )

    override fun parseResult(resultCode: Int, intent: Intent?): (ContentResolver) -> Uri? =
        { contentResolver ->
            intent?.data
                ?.also {
                    contentResolver.takePersistableUriPermission(
                        it,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION,
                    )
                }
        }
}
