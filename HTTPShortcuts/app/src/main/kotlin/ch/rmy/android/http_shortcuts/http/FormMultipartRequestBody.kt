package ch.rmy.android.http_shortcuts.http

import ch.rmy.android.http_shortcuts.http.RequestUtil.FORM_MULTIPART_CONTENT_TYPE
import ch.rmy.android.http_shortcuts.http.RequestUtil.sanitize
import java.io.InputStream
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source

class FormMultipartRequestBody(private val parameters: List<RequestBuilder.Parameter>) : RequestBody() {

    override fun contentType(): MediaType =
        RequestUtil.getMediaType(FORM_MULTIPART_CONTENT_TYPE)

    private val length: Long by lazy {
        try {
            var computedLength = 0L
            process(
                writeString = { string ->
                    computedLength += string.toByteArray().size
                },
                writeStream = { _, length ->
                    computedLength += length ?: throw UnknownLength()
                },
            )
            computedLength
        } catch (t: UnknownLength) {
            -1
        }
    }

    override fun contentLength(): Long = length

    override fun writeTo(sink: BufferedSink) {
        process(
            writeString = { string ->
                sink.writeUtf8(string)
            },
            writeStream = { stream, _ ->
                sink.writeAll(stream.source())
            },
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
