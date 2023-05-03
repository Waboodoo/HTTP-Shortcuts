package ch.rmy.android.http_shortcuts.activities.response

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity.RESULT_OK

object SaveFileContract : ActivityResultContract<SaveFileContract.Params, Uri?>() {

    override fun createIntent(context: Context, input: Params): Intent =
        Intent(Intent.ACTION_CREATE_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType(input.type)
            .putExtra(Intent.EXTRA_TITLE, input.title)

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? =
        intent?.takeIf { resultCode == RESULT_OK }?.data

    data class Params(val type: String?, val title: String)
}
