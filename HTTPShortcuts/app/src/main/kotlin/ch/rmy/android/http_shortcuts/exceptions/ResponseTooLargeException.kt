package ch.rmy.android.http_shortcuts.exceptions

import android.content.Context
import android.text.format.Formatter
import ch.rmy.android.http_shortcuts.R

class ResponseTooLargeException(private val limit: Long) : UserException() {

    override fun getLocalizedMessage(context: Context): String =
        context.getString(R.string.error_response_too_large, Formatter.formatShortFileSize(context, limit))
}
