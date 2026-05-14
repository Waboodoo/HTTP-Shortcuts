package ch.rmy.android.http_shortcuts.http

import java.io.IOException
import okhttp3.RequestBody
import okio.Buffer
import okio.BufferedSink
import okio.ForwardingSink
import okio.buffer

class ProgressRequestBody(
    private val delegate: RequestBody,
    private val onUploadProgress: (UploadProgress) -> Unit,
) : RequestBody() {

    override fun contentType() = delegate.contentType()

    @Throws(IOException::class)
    override fun contentLength() = delegate.contentLength()

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        val forwardingSink = object : ForwardingSink(sink) {
            private var totalBytesWritten: Long = 0
            private var completed = false

            override fun write(source: Buffer, byteCount: Long) {
                super.write(source, byteCount)
                totalBytesWritten += byteCount
                val progress = if (completed) {
                    1f
                } else {
                    val total = contentLength()
                    if (total <= 0) {
                        return
                    }
                    (totalBytesWritten.toFloat()) / total
                }
                onUploadProgress(UploadProgress(progress))
            }

            override fun close() {
                super.close()
                if (!completed) {
                    completed = true
                    onUploadProgress(UploadProgress(1f))
                }
            }
        }

        val bufferedSink = forwardingSink.buffer()
        delegate.writeTo(bufferedSink)
        bufferedSink.flush()
    }
}
