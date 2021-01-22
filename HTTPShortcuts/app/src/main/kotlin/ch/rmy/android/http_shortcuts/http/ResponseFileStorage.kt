package ch.rmy.android.http_shortcuts.http

import android.content.Context
import android.net.Uri
import ch.rmy.android.http_shortcuts.extensions.mapIf
import ch.rmy.android.http_shortcuts.utils.FileUtil
import okhttp3.Response
import java.io.InputStream
import java.util.zip.GZIPInputStream

class ResponseFileStorage(private val context: Context, private val id: String) {

    fun store(response: Response): Uri {
        val file = FileUtil.createCacheFile(context, "response_$id")
        getStream(response).use { inStream ->
            context.contentResolver.openOutputStream(file, "w")!!.use { outStream ->
                inStream.copyTo(outStream)
            }
        }
        return file
    }

    private fun getStream(response: Response): InputStream =
        response.body()!!.byteStream()
            .mapIf(isGzipped(response)) {
                GZIPInputStream(it)
            }

    companion object {
        private fun isGzipped(response: Response): Boolean =
            response.header(HttpHeaders.CONTENT_ENCODING) == "gzip"
    }

}