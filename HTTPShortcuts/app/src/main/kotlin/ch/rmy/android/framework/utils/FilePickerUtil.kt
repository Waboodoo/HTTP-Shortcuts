package ch.rmy.android.framework.utils

import android.content.Intent
import android.net.Uri

object FilePickerUtil {

    fun createIntent(multiple: Boolean = false, type: String = "*/*"): Intent =
        Intent(Intent.ACTION_OPEN_DOCUMENT)
            .apply {
                this.type = type
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, multiple)
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
