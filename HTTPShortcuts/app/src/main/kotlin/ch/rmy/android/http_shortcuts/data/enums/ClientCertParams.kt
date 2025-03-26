package ch.rmy.android.http_shortcuts.data.enums

import android.content.Context

sealed interface ClientCertParams {
    data class Alias(val alias: String) : ClientCertParams {
        override fun toString() =
            "$CLIENT_CERT_ALIAS_PREFIX$alias"
    }

    data class File(val fileName: String, val password: String) : ClientCertParams {
        override fun toString() =
            "$CLIENT_CERT_FILE_PREFIX$fileName$CLIENT_CERT_FILE_PASSWORD_DELIMITER$password"

        fun getFile(context: Context): java.io.File =
            context.getFileStreamPath(fileName)
    }

    companion object {
        fun parse(string: String) =
            when {
                string.startsWith(CLIENT_CERT_ALIAS_PREFIX) ->
                    Alias(string.removePrefix(CLIENT_CERT_ALIAS_PREFIX))
                string.startsWith(CLIENT_CERT_FILE_PREFIX) && string.contains(CLIENT_CERT_FILE_PASSWORD_DELIMITER) ->
                    File(
                        fileName = string.removePrefix(CLIENT_CERT_FILE_PREFIX)
                            .takeWhile { it != CLIENT_CERT_FILE_PASSWORD_DELIMITER },
                        password = string.removePrefix(CLIENT_CERT_FILE_PREFIX)
                            .dropWhile { it != CLIENT_CERT_FILE_PASSWORD_DELIMITER }
                            .drop(1),
                    )
                else -> null
            }

        private const val CLIENT_CERT_ALIAS_PREFIX = "alias:"
        private const val CLIENT_CERT_FILE_PREFIX = "file:"
        private const val CLIENT_CERT_FILE_PASSWORD_DELIMITER = ';'
    }
}
