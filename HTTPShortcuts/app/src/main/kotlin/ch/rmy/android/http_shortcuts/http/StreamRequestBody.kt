package ch.rmy.android.http_shortcuts.http

import ch.rmy.android.http_shortcuts.http.RequestUtil.getMediaType
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import okio.Okio
import java.io.InputStream

class StreamRequestBody(private val contentType: String?, private val stream: InputStream) : RequestBody() {
    override fun contentType(): MediaType? =
        getMediaType(contentType)

    override fun writeTo(sink: BufferedSink) {
        sink.writeAll(Okio.source(stream))
    }

}