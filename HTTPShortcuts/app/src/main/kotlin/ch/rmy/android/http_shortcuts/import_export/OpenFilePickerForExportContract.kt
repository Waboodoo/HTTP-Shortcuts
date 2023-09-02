package ch.rmy.android.http_shortcuts.import_export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract

object OpenFilePickerForExportContract : ActivityResultContract<OpenFilePickerForExportContract.Params, Uri?>() {
    override fun createIntent(context: Context, input: Params): Intent =
        Intent(Intent.ACTION_CREATE_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType(ExportFormat.ZIP.fileType)
            .putExtra(Intent.EXTRA_TITLE, ExportFormat.ZIP.getFileName(input.single))

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? =
        intent?.data

    data class Params(
        val single: Boolean = false,
    )
}
