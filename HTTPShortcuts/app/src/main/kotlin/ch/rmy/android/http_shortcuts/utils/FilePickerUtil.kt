package ch.rmy.android.http_shortcuts.utils

import android.content.Intent
import android.net.Uri
import android.os.Build

object FilePickerUtil {

    fun createIntent(multiple: Boolean = false, type: String = "*/*"): Intent =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent(Intent.ACTION_OPEN_DOCUMENT)
        } else {
            Intent(Intent.ACTION_GET_CONTENT)
        }
            .apply {
                this.type = type
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, multiple)
                }
                addCategory(Intent.CATEGORY_OPENABLE)
            }

    fun extractUris(intent: Intent): List<Uri>? =
        intent.clipData
            ?.let { data ->
                mutableListOf<Uri>().apply {
                    for (i in 0 until data.itemCount) {
                        val uri = data.getItemAt(i).uri
                        add(uri)
                    }
                }
            }
            ?: intent.data?.let { listOf(it) }

}