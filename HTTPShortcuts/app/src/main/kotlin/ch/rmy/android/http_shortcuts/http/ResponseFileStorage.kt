package ch.rmy.android.http_shortcuts.http

import android.content.Context
import android.net.Uri
import ch.rmy.android.framework.extensions.mapIf
import ch.rmy.android.http_shortcuts.utils.FileUtil
import okhttp3.Response
import java.io.File
import java.io.InputStream
import java.util.zip.GZIPInputStream

class ResponseFileStorage(private val context: Context, private val id: String) {

    private val file by lazy {
        File(context.cacheDir, "response_$id")
    }

    fun store(response: Response): Uri {
        val fileUri = FileUtil.getUriFromFile(context, file)
        getStream(response).use { inStream ->
            context.contentResolver.openOutputStream(fileUri, "w")!!.use { outStream ->
                inStream.copyTo(outStream)
            }
        }
        return fileUri
    }

    private fun getStream(response: Response): InputStream =
        response.body!!.byteStream()
            .mapIf(isGzipped(response)) {
                GZIPInputStream(this)
            }

    fun clear() {
        file.delete()
    }

    companion object {
        private fun isGzipped(response: Response): Boolean =
            response.header(HttpHeaders.CONTENT_ENCODING) == "gzip"
    }
}
