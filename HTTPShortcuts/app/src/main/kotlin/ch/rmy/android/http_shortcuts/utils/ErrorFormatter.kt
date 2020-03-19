package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.text.format.Formatter
import androidx.annotation.StringRes
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.exceptions.InvalidUrlException
import ch.rmy.android.http_shortcuts.extensions.logException
import ch.rmy.android.http_shortcuts.http.ErrorResponse

class ErrorFormatter(private val context: Context) {

    fun getPrettyError(error: Throwable, shortcutName: String, includeBody: Boolean): String =
        if (error is ErrorResponse) {
            getHttpErrorMessage(error, shortcutName, includeBody)
        } else {
            String.format(getString(R.string.error_other), shortcutName, getErrorMessage(error))
        }

    private fun getHttpErrorMessage(error: ErrorResponse, shortcutName: String, includeBody: Boolean): String {
        val builder = StringBuilder()
        builder.append(String.format(getString(R.string.error_http), shortcutName, error.shortcutResponse.statusCode))

        if (includeBody && error.shortcutResponse.bodyAsString.isNotEmpty()) {
            try {
                builder.append("\n\n")
                builder.append(error.shortcutResponse.bodyAsString)
            } catch (e: Exception) {
                logException(e)
            }
        }
        return builder.toString()
    }

    private fun getErrorMessage(error: Throwable): String =
        when {
            error is InvalidUrlException -> getString(R.string.error_invalid_url)
            error is SizeLimitedReader.LimitReachedException -> context.getString(R.string.error_response_too_large, Formatter.formatShortFileSize(context, error.limit))
            error.cause?.message != null -> error.cause!!.message!!
            error.message != null -> error.message!!
            else -> error.javaClass.simpleName
        }

    private fun getString(@StringRes stringRes: Int): String = context.getString(stringRes)

}