package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.util.Base64
import androidx.annotation.StringRes
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.extensions.toChunkedHexString
import ch.rmy.android.framework.extensions.truncate
import ch.rmy.android.framework.extensions.tryOrLog
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.exceptions.ResponseTooLargeException
import ch.rmy.android.http_shortcuts.exceptions.TreatAsFailureException
import ch.rmy.android.http_shortcuts.exceptions.UserException
import ch.rmy.android.http_shortcuts.http.ErrorResponse
import ch.rmy.android.http_shortcuts.http.HttpStatus
import java.net.ConnectException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.net.ssl.SSLPeerUnverifiedException

class ErrorFormatter
@Inject
constructor(
    private val context: Context,
) {

    fun getPrettyError(error: Throwable, shortcutName: String, includeBody: Boolean = false): String =
        when (error) {
            is ErrorResponse -> {
                getHttpErrorMessage(error, shortcutName, includeBody)
            }
            is UserException -> {
                getErrorMessage(error)
            }
            is TreatAsFailureException -> {
                error.message ?: getString(R.string.error_user_defined)
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
            ),
        )

        if (includeBody) {
            try {
                tryOrLog {
                    val responseBody = try {
                        error.shortcutResponse.getContentAsString(context)
                    } catch (e: ResponseTooLargeException) {
                        e.getLocalizedMessage(context)
                    }
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

    fun getErrorMessage(error: Throwable): String =
        when (error) {
            is UserException -> error.getLocalizedMessage(context)
            is ConnectException,
            is UnknownHostException,
            -> error.message!!
            is SSLPeerUnverifiedException -> formatSSLPeerUnverifiedException(error)
            else -> getUnknownErrorMessage(error)
        }

    private fun formatSSLPeerUnverifiedException(error: SSLPeerUnverifiedException): String =
        getSingleErrorMessage(error).replace("(sha1|256)/([^=]+=):?".toRegex()) { match ->
            val (algorithm, base64hash) = match.destructured
            try {
                val formattedHash = Base64.decode(base64hash, Base64.DEFAULT)
                    .toChunkedHexString()
                "${algorithm.uppercase()}/$formattedHash"
            } catch (e: IllegalArgumentException) {
                match.value
            }
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
            .run {
                val messages = mutableListOf<String>()
                forEach { message ->
                    if (messages.none { it in message || message in it }) {
                        messages.add(message)
                    }
                }
                messages
            }
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
