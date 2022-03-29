package ch.rmy.android.http_shortcuts.icons

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import ch.rmy.android.http_shortcuts.R

object IpackPickerContract : ActivityResultContract<Unit, ShortcutIcon.ExternalResourceIcon?>() {
    override fun createIntent(context: Context, input: Unit?): Intent =
        getIpackIntent(context)

    override fun parseResult(resultCode: Int, intent: Intent?): ShortcutIcon.ExternalResourceIcon? =
        if (resultCode == AppCompatActivity.RESULT_OK && intent != null) {
            ShortcutIcon.ExternalResourceIcon(getIpackUri(intent))
        } else null

    private const val PACKAGE_NAME = "net.dinglisch.android.ipack"
    private const val ACTION_SELECT_SUFFIX = ".actions.ICON_SELECT"
    private const val ICON_ID_SUFFIX = ".extras.ICON_ID"

    private fun getIpackIntent(context: Context) =
        Intent.createChooser(Intent(PACKAGE_NAME + ACTION_SELECT_SUFFIX), context.getString(R.string.choose_ipack))!!

    private fun getIpackUri(intent: Intent): Uri {
        val packageName = intent.data!!.authority
        val id = intent.getIntExtra(PACKAGE_NAME + ICON_ID_SUFFIX, -1)
        return "android.resource://$packageName/$id".toUri()
    }
}
