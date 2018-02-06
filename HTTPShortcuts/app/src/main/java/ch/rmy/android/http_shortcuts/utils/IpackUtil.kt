package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import ch.rmy.android.http_shortcuts.R

object IpackUtil {

    private const val PACKAGE_NAME = "net.dinglisch.android.ipack"
    private const val ACTION_SELECT_SUFFIX = ".actions.ICON_SELECT"
    private const val ICON_ID_SUFFIX = ".extras.ICON_ID"

    fun getIpackIntent(context: Context) =
            Intent.createChooser(Intent(PACKAGE_NAME + ACTION_SELECT_SUFFIX), context.getString(R.string.choose_ipack))!!

    fun getIpackUri(intent: Intent): Uri {
        val packageName = intent.data.authority
        val id = intent.getIntExtra(PACKAGE_NAME + ICON_ID_SUFFIX, -1)
        return Uri.parse("android.resource://$packageName/$id")
    }

}