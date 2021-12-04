package ch.rmy.android.http_shortcuts.exceptions

import android.content.Context
import ch.rmy.android.http_shortcuts.R

class InvalidUrlException(val url: String, private val detail: String? = null) : UserException() {

    override fun getLocalizedMessage(context: Context): String =
        context.getString(R.string.error_invalid_url, detail ?: url)
}
