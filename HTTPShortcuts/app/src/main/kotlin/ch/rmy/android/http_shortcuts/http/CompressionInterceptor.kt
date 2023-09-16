package ch.rmy.android.http_shortcuts.http

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.asResponseBody
import okhttp3.internal.http.promisesBody
import okio.GzipSource
import okio.buffer
import okio.source
import org.brotli.dec.BrotliInputStream

object CompressionInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(getFinalRequest(chain.request()))
        return uncompressIfNeeded(response)
    }

    private fun getFinalRequest(request: Request): Request =
        if (request.header("Accept-Encoding") == null && request.header("Range") == null) {
            request.newBuilder()
                .header("Accept-Encoding", "br,gzip")
                .build()
        } else {
            request
        }

    private fun uncompressIfNeeded(response: Response): Response {
        val body = response.takeIf { it.promisesBody() }?.body
            ?: return response
        val encoding = response.header("Content-Encoding")
            ?: return response

        val decompressedSource = when {
            encoding.equals("br", ignoreCase = true) -> {
                BrotliInputStream(body.source().inputStream()).source().buffer()
            }
            encoding.equals("gzip", ignoreCase = true) -> {
                GzipSource(body.source()).buffer()
            }
            else -> return response
        }

        return response.newBuilder()
            .removeHeader("Content-Encoding")
            .removeHeader("Content-Length")
            .body(decompressedSource.asResponseBody(body.contentType(), -1))
            .build()
    }
}
