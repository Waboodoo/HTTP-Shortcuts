package ch.rmy.android.http_shortcuts.http

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.http.HttpRequester.Companion.isStreaming
import ch.rmy.android.http_shortcuts.http.HttpRequester.Companion.isUnknownLength
import java.io.File
import java.io.InputStream
import java.net.SocketTimeoutException
import okhttp3.Response

class ResponseFileStorage(
    private val context: Context,
    private val sessionId: String,
    private val storeDirectoryUri: Uri?,
) {

    fun store(
        response: Response,
        finishNormallyOnTimeout: Boolean = response.isStreaming() || response.isUnknownLength(),
    ): DocumentFile {
        val fileName = "response_$sessionId"

        val documentFile = storeDirectoryUri
            ?.let {
                val directory = DocumentFile.fromTreeUri(context, it)
                directory?.createFile(response.getMimeType(), fileName)
            }
            ?: run {
                val file = File(context.cacheDir, fileName)
                DocumentFile.fromFile(file)
            }

        try {
            getStream(response).use { inStream ->
                context.contentResolver.openOutputStream(documentFile.uri, "w")!!.use { outStream ->
                    inStream.copyTo(outStream)
                }
            }
        } catch (e: SocketTimeoutException) {
            if (!finishNormallyOnTimeout) {
                throw e
            }
        }
        return documentFile
    }

    private fun getStream(response: Response): InputStream =
        response.body.byteStream()

    companion object {
        internal fun Response.getMimeType(): String =
            header(HttpHeaders.CONTENT_TYPE)
                ?.let { contentType ->
                    contentType.split(';', limit = 2)[0]
                }
                ?.takeUnlessEmpty()
                ?.lowercase()
                ?.trim()
                ?: "application/octet-stream"
    }
}
