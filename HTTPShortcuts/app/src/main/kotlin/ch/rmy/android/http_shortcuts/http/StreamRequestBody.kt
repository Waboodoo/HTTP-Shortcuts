package ch.rmy.android.http_shortcuts.http

import ch.rmy.android.http_shortcuts.http.RequestUtil.getMediaType
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source
import java.io.InputStream

class StreamRequestBody(
    private val contentType: String?,
    private val stream: InputStream,
    private val length: Long? = null,
) : RequestBody() {

    override fun contentLength(): Long =
        length ?: -1

    override fun contentType(): MediaType =
        getMediaType(contentType)

    override fun writeTo(sink: BufferedSink) {
        sink.writeAll(stream.source())
    }
}
