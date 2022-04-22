package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import androidx.annotation.StringRes
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.extensions.truncate
import ch.rmy.android.framework.extensions.tryOrLog
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.exceptions.ResponseTooLargeException
import ch.rmy.android.http_shortcuts.exceptions.UserException
import ch.rmy.android.http_shortcuts.http.ErrorResponse
import ch.rmy.android.http_shortcuts.http.HttpStatus
import io.reactivex.exceptions.CompositeException
import java.net.ConnectException
import java.net.UnknownHostException

class ErrorFormatter(private val context: Context) {

    fun getPrettyError(error: Throwable, shortcutName: String, includeBody: Boolean): String =
        when (error) {
            is ErrorResponse -> {
                getHttpErrorMessage(error, shortcutName, includeBody)
            }
            is UserException -> {
                getErrorMessage(error)
            }
            else -> {
                String.format(getString(R.string.error_other), shortcutName, getErrorMessage(error))
            }
        }

    private fun getHttpErrorMessage(error: ErrorResponse, shortcutName: String, includeBody: Boolean): String {
        val builder = StringBuilder()
        builder.append(
            String.format(
                getString(R.string.error_http),
                shortcutName,
                error.shortcutResponse.statusCode,
                HttpStatus.getMessage(error.shortcutResponse.statusCode),
            )
        )

        if (includeBody) {
            try {
                tryOrLog {
                    val responseBody = error.shortcutResponse.getContentAsString(context)
                    if (responseBody.isNotEmpty()) {
                        builder.append("\n\n")
                        builder.append(responseBody.truncate(MAX_ERROR_LENGTH))
                    }
                }
            } catch (e: ResponseTooLargeException) {
                builder.append("\n\n")
                builder.append(e.getLocalizedMessage(context))
            }
        }
        return builder.toString()
    }

    private fun getErrorMessage(error: Throwable): String =
        when (error) {
            is CompositeException -> error.exceptions.joinToString(separator = "\n") { getErrorMessage(it) }
            is UserException -> error.getLocalizedMessage(context)
            is ConnectException,
            is UnknownHostException,
            -> error.message!!
            else -> getUnknownErrorMessage(error)
        }

    private fun getUnknownErrorMessage(error: Throwable): String =
        getCauseChain(error)
            .let {
                if (it.size > 1 && it[0] is RuntimeException) {
                    it.drop(1)
                } else {
                    it
                }
            }
            .map(::getSingleErrorMessage)
            .distinct()
            .joinToString(separator = "\n")

    private fun getCauseChain(error: Throwable, recursionDepth: Int = 0): List<Throwable> =
        listOf(error)
            .runIf(error.cause != null && error.cause != error && recursionDepth < MAX_RECURSION_DEPTH) {
                plus(getCauseChain(error.cause!!, recursionDepth + 1))
            }

    private fun getSingleErrorMessage(error: Throwable): String =
        when {
            error.message != null -> error.message!!
            else -> context.getString(R.string.error_generic)
        }

    private fun getString(@StringRes stringRes: Int): String = context.getString(stringRes)

    companion object {
        private const val MAX_RECURSION_DEPTH = 2
        private const val MAX_ERROR_LENGTH = 10000
    }
}
