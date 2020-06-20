package ch.rmy.android.http_shortcuts.http

import ch.rmy.android.http_shortcuts.http.RequestUtil.FORM_MULTIPART_CONTENT_TYPE
import ch.rmy.android.http_shortcuts.http.RequestUtil.sanitize
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import okio.Okio
import java.io.InputStream

class FormMultipartRequestBody(private val parameters: List<RequestBuilder.Parameter>) : RequestBody() {

    override fun contentType(): MediaType =
        RequestUtil.getMediaType(FORM_MULTIPART_CONTENT_TYPE)

    override fun contentLength(): Long {
        try {
            var computedLength = 0L
            process(
                { string ->
                    computedLength += string.length
                }, { _, length ->
                computedLength += length ?: throw UnknownLength()
            }
            )
            return computedLength
        } catch (t: UnknownLength) {
            return -1
        }
    }

    override fun writeTo(sink: BufferedSink) {
        process(
            { string ->
                sink.writeUtf8(string)
            },
            { stream, _ ->
                sink.writeAll(Okio.source(stream))
            }
        )
    }

    private fun process(writeString: (String) -> Unit, writeStream: (InputStream, Long?) -> Unit) {
        writeString("\r\n")
        parameters.forEach { parameter ->
            writeString("\r\n--${RequestUtil.FORM_MULTIPART_BOUNDARY}\r\n")
            when (parameter) {
                is RequestBuilder.Parameter.StringParameter -> {
                    writeString("Content-Disposition: form-data; name=\"${sanitize(parameter.name)}\"")
                    writeString("\r\n\r\n")
                    writeString(parameter.value)
                }
                is RequestBuilder.Parameter.FileParameter -> {
                    writeString("Content-Disposition: form-data; name=\"${sanitize(parameter.name)}\"; filename=\"${sanitize(parameter.fileName)}\"")
                    writeString("\r\n")
                    writeString("Content-Type: ${parameter.type}")
                    writeString("\r\n\r\n")
                    writeStream(parameter.data, parameter.length)
                }
            }
        }
        writeString("\r\n--${RequestUtil.FORM_MULTIPART_BOUNDARY}--\r\n")
    }

    class UnknownLength : Throwable()

}